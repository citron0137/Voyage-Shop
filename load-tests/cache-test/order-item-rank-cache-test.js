import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 테스트 설정 및 환경 변수
const API_HOST = __ENV.API_HOST || 'localhost:8080';
const BASE_URL = `http://${API_HOST}/api/v1`;
const ORDER_ITEM_RANK_URL = `${BASE_URL}/order-item-rank`;

// 커스텀 메트릭 정의
const cacheHitRate = new Rate('cache_hit_rate');
const cacheMissRate = new Rate('cache_miss_rate');
const cacheVsDirectTimeRatio = new Trend('cache_vs_direct_time_ratio');
const cacheHitResponseTime = new Trend('cache_hit_response_time');
const cacheMissResponseTime = new Trend('cache_miss_response_time');
const cacheRefreshImpact = new Trend('cache_refresh_impact');

// 테스트 시나리오 정의
export const options = {
  scenarios: {
    // 캐시 효율성 테스트: 일반 부하 시나리오
    cache_efficiency: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 200,
      stages: [
        { duration: '30s', target: 30 }, // 워밍업
        { duration: '1m', target: 50 },  // 일반 부하
        { duration: '30s', target: 20 }, // 냉각
      ],
    },
    
    // 캐시 갱신 시 영향 테스트: 데이터 초기화 전/후 응답 시간 비교
    cache_refresh_impact: {
      executor: 'constant-arrival-rate',
      rate: 30,
      timeUnit: '1s',
      duration: '1m',
      preAllocatedVUs: 30,
      maxVUs: 50,
      startTime: '2m', // 첫 번째 시나리오 이후 시작
      exec: 'refreshCacheTest',
    },
  },
  thresholds: {
    'cache_hit_rate': ['rate>0.75'], // 캐시 히트율 75% 이상
    'cache_hit_response_time': ['p(95)<50'], // 캐시 히트 시 95% 응답시간 50ms 이내
    'http_req_failed': ['rate<0.01'], // 요청 실패율 1% 미만
  },
};

// 매 요청마다 요청 간격을 다르게 설정
function getRequestDelay() {
  // 0: 빠른 연속 요청 (캐시 히트 가능성 높음)
  // 1: 중간 간격 요청 (캐시 가능성 중간)
  // 2: 긴 간격 요청 (캐시 미스 가능성 높음)
  const delayType = randomIntBetween(0, 2);
  
  switch (delayType) {
    case 0: return randomIntBetween(1, 3) / 100; // 0.01~0.03초 (빠른 연속 요청)
    case 1: return randomIntBetween(2, 5) / 10;  // 0.2~0.5초 (중간 간격)
    case 2: return randomIntBetween(1, 3);       // 1~3초 (긴 간격)
  }
}

// 캐시 히트/미스 여부 추정
// 참고: 실제 캐시 히트/미스는 응답 헤더나 기타 정보로 정확히 판단해야 하지만,
// 여기서는 응답 시간을 기준으로 히트/미스를 추정함
function estimateCacheStatus(responseTime) {
  // 임계값: 20ms 이하면 캐시 히트로 간주 (실제 환경에 맞게 조정 필요)
  const isCacheHit = responseTime <= 20;
  
  // 캐시 히트/미스 메트릭 기록
  cacheHitRate.add(isCacheHit);
  cacheMissRate.add(!isCacheHit);
  
  return {
    isCacheHit,
    responseTime
  };
}

// 기본 테스트 함수 (캐시 효율성 시나리오)
export default function() {
  // API 호출
  const response = http.get(ORDER_ITEM_RANK_URL, {
    tags: { name: 'RankCacheAPI' }
  });
  
  // 응답 검증
  const success = check(response, {
    'API 응답 성공 (200)': (r) => r.status === 200,
    '응답에 데이터 포함': (r) => {
      try {
        const data = JSON.parse(r.body);
        return data && data.data && Array.isArray(data.data);
      } catch (e) {
        return false;
      }
    },
  });
  
  if (success) {
    // 캐시 상태 추정
    const responseTime = response.timings.duration;
    const { isCacheHit } = estimateCacheStatus(responseTime);
    
    // 캐시 상태에 따른 응답 시간 기록
    if (isCacheHit) {
      cacheHitResponseTime.add(responseTime);
    } else {
      cacheMissResponseTime.add(responseTime);
      
      // 캐시 미스 후 즉시 재요청하여 캐시 히트 확인
      sleep(0.1); // 0.1초 대기
      
      const secondResponse = http.get(ORDER_ITEM_RANK_URL, {
        tags: { name: 'RankCacheAPI_Repeat' }
      });
      
      const secondResponseTime = secondResponse.timings.duration;
      const secondCacheStatus = estimateCacheStatus(secondResponseTime);
      
      // 첫 요청과 두 번째 요청의 시간 비율 계산 (캐시 효과 측정)
      if (secondCacheStatus.isCacheHit) {
        const timeRatio = secondResponseTime / responseTime;
        cacheVsDirectTimeRatio.add(timeRatio);
      }
    }
  }
  
  // 요청 간 랜덤 지연
  sleep(getRequestDelay());
}

// 캐시 갱신 영향 테스트 함수
export function refreshCacheTest() {
  // 단계 1: 캐시가 채워진 상태 확인
  const initialResponse = http.get(ORDER_ITEM_RANK_URL);
  const initialTime = initialResponse.timings.duration;
  
  // 응답 시간 측정
  console.log(`캐시 갱신 전 응답 시간: ${initialTime}ms`);
  
  // 여러 번 요청하여 평균 응답 시간 측정
  let totalTimeBefore = initialTime;
  const requestCount = 5;
  
  for (let i = 0; i < requestCount - 1; i++) {
    sleep(0.2);
    const response = http.get(ORDER_ITEM_RANK_URL);
    totalTimeBefore += response.timings.duration;
  }
  
  const avgTimeBefore = totalTimeBefore / requestCount;
  
  // 단계 2: 캐시 갱신 (데이터 초기화 API 호출)
  console.log('캐시 초기화 요청 (DELETE 호출)');
  const resetResponse = http.del(ORDER_ITEM_RANK_URL);
  
  check(resetResponse, {
    '초기화 API 응답 성공 (200)': (r) => r.status === 200
  });
  
  // 단계 3: 캐시 갱신 직후 요청
  sleep(0.1); // 100ms 대기
  const duringRefreshResponse = http.get(ORDER_ITEM_RANK_URL);
  const duringRefreshTime = duringRefreshResponse.timings.duration;
  
  // 단계 4: 캐시 갱신 후 일정 시간 후 요청
  sleep(1); // 1초 대기
  
  // 여러 번 요청하여 평균 응답 시간 측정
  let totalTimeAfter = 0;
  
  for (let i = 0; i < requestCount; i++) {
    const response = http.get(ORDER_ITEM_RANK_URL);
    totalTimeAfter += response.timings.duration;
    sleep(0.2);
  }
  
  const avgTimeAfter = totalTimeAfter / requestCount;
  
  // 갱신 전/중/후 응답 시간 비율 계산
  const duringRefreshRatio = duringRefreshTime / avgTimeBefore;
  const afterRefreshRatio = avgTimeAfter / avgTimeBefore;
  
  // 메트릭 기록
  cacheRefreshImpact.add(duringRefreshRatio, { phase: 'during' });
  cacheRefreshImpact.add(afterRefreshRatio, { phase: 'after' });
  
  // 로그 출력
  console.log(`캐시 갱신 영향 측정:
  - 갱신 전 평균 응답 시간: ${avgTimeBefore.toFixed(2)}ms
  - 갱신 중 응답 시간: ${duringRefreshTime.toFixed(2)}ms (${(duringRefreshRatio * 100).toFixed(2)}%)
  - 갱신 후 평균 응답 시간: ${avgTimeAfter.toFixed(2)}ms (${(afterRefreshRatio * 100).toFixed(2)}%)
  `);
  
  // 요청 간 지연
  sleep(randomIntBetween(3, 8) / 10); // 0.3 ~ 0.8초 지연
} 