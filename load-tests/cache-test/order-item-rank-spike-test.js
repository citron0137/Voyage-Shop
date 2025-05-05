import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 테스트 설정 및 환경 변수
const API_HOST = __ENV.API_HOST || 'localhost:8080';
const BASE_URL = `http://${API_HOST}/api/v1`;
const ORDER_ITEM_RANK_URL = `${BASE_URL}/order-item-rank`;

// 커스텀 메트릭 정의
const peakResponseTime = new Trend('peak_response_time');
const errorRateUnderLoad = new Rate('error_rate_under_load');
const recoveryTime = new Trend('recovery_time');
const rankConsistency = new Rate('rank_consistency');
const requestsPerPhase = new Counter('requests_per_phase');

// 인기 카테고리 목록 (스파이크 트래픽을 집중시킬 대상)
const popularCategories = [
  'electronics',
  'clothing',
  'home',
  'beauty',
  'sports'
];

// 정렬 옵션
const sortOptions = [
  { field: 'popularity', direction: 'desc' },
  { field: 'price', direction: 'asc' },
  { field: 'rating', direction: 'desc' }
];

// 테스트 시나리오 정의
export const options = {
  scenarios: {
    // 기본 부하 단계 - 정상 트래픽 시뮬레이션
    normal_traffic: {
      executor: 'constant-arrival-rate',
      rate: 20,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 20,
      maxVUs: 50,
      exec: 'normalTraffic',
      tags: { phase: 'normal' }
    },
    
    // 갑작스러운 트래픽 증가 - 스파이크 테스트
    traffic_spike: {
      executor: 'ramping-arrival-rate',
      startRate: 20,
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 500,
      stages: [
        { duration: '10s', target: 100 },  // 10초 동안 5배 급증
        { duration: '20s', target: 200 },  // 20초 동안 10배 급증
        { duration: '5s', target: 20 },    // 5초 동안 정상화
      ],
      startTime: '30s',  // 기본 부하 단계 후 시작
      exec: 'spikeTraffic',
      tags: { phase: 'spike' }
    },
    
    // 복구 단계 - 스파이크 후 시스템 복구 모니터링
    recovery_phase: {
      executor: 'constant-arrival-rate',
      rate: 20,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 20,
      maxVUs: 50,
      startTime: '65s',  // 스파이크 단계 후 시작
      exec: 'recoveryTraffic',
      tags: { phase: 'recovery' }
    },
    
    // 데이터 초기화 단계 - 테스트 마지막에 한 번만 실행
    reset_data: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: '5s',
      startTime: '95s', // 복구 단계 종료 후 시작
      exec: 'resetData',
      tags: { phase: 'reset' }
    }
  },
  thresholds: {
    'peak_response_time': ['p(99)<1000'], // 최대 부하 상황에서 99%의 요청이 1초 이내 처리
    'error_rate_under_load': ['rate<0.05'], // 고부하 상황에서 에러율 5% 미만
    'recovery_time': ['max<5000'], // 복구 시간 5초 이내
    'rank_consistency': ['rate>0.95'], // 순위 일관성 95% 이상
  },
};

// 성능 테스트를 위한 기준 데이터 저장
let baselineResponseTimes = [];
let lastResponseResults = [];
let testStartTime;

// 테스트 시작 설정
export function setup() {
  console.log('스파이크 테스트 준비 중... API 엔드포인트:', ORDER_ITEM_RANK_URL);
  testStartTime = new Date().getTime();
  return { testStartTime };
}

// 쿼리 파라미터 생성
function buildQueryParams(category, sort, additionalParams = {}) {
  const params = new URLSearchParams();
  
  // 카테고리 설정
  if (category) {
    params.append('category', category);
  }
  
  // 정렬 설정
  if (sort) {
    params.append('sortBy', sort.field);
    params.append('sortDirection', sort.direction);
  }
  
  // 추가 파라미터
  Object.entries(additionalParams).forEach(([key, value]) => {
    params.append(key, value);
  });
  
  // 기본 페이징
  params.append('page', 1);
  params.append('size', 20);
  
  return params.toString();
}

// 요청 실행 및 메트릭 수집 함수
function executeRequest(phase) {
  // API 호출
  const response = http.get(ORDER_ITEM_RANK_URL, {
    tags: { phase: phase }
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
  
  // 요청 카운터 증가
  requestsPerPhase.add(1, { phase: phase });
  
  // 에러율 계산 (스파이크 단계에서만)
  if (phase === 'spike') {
    errorRateUnderLoad.add(success ? 0 : 1);
  }
  
  // 응답 시간 기록
  if (phase === 'spike') {
    peakResponseTime.add(response.timings.duration);
  }
  
  // 순위 일관성 검증
  if (success) {
    try {
      const responseData = JSON.parse(response.body);
      const currentResults = responseData.data.map(item => item.itemId).join(',');
      
      if (lastResponseResults.length > 0) {
        const previousResults = lastResponseResults[lastResponseResults.length - 1];
        const isConsistent = previousResults === currentResults;
        rankConsistency.add(isConsistent);
        
        if (!isConsistent && phase !== 'spike') {
          console.log(`순위 일관성 변화 감지 (${phase} 단계)`);
        }
      }
      
      // 최대 5개까지만 저장
      if (lastResponseResults.length >= 5) {
        lastResponseResults.shift(); // 가장 오래된 결과 제거
      }
      lastResponseResults.push(currentResults);
      
      // 기준 응답 시간 저장 (정상 트래픽 단계에서만)
      if (phase === 'normal') {
        baselineResponseTimes.push(response.timings.duration);
      }
      
      // 복구 시간 측정 (복구 단계에서만)
      if (phase === 'recovery' && baselineResponseTimes.length > 0) {
        // 기준 응답 시간 평균 계산
        const avgBaselineTime = baselineResponseTimes.reduce((a, b) => a + b, 0) / baselineResponseTimes.length;
        const currentTime = response.timings.duration;
        const recoveryRatio = currentTime / avgBaselineTime;
        
        // 복구 비율이 1.5배 이내면 복구된 것으로 간주
        if (recoveryRatio <= 1.5) {
          const elapsedSinceSpike = new Date().getTime() - (testStartTime + 65000); // 65초는 스파이크 종료 시점
          recoveryTime.add(elapsedSinceSpike);
        }
      }
    } catch (e) {
      console.error('응답 데이터 처리 오류:', e);
    }
  }
  
  return {
    success,
    response
  };
}

// 정상 트래픽 시뮬레이션 (기본 단계)
export function normalTraffic() {
  // 랜덤 카테고리 및 정렬 선택
  const category = popularCategories[randomIntBetween(0, popularCategories.length - 1)];
  const sort = sortOptions[randomIntBetween(0, sortOptions.length - 1)];
  
  // 쿼리 파라미터 및 URL 생성
  const queryParams = buildQueryParams(category, sort);
  const url = `${BASE_URL}/items/rank?${queryParams}`;
  
  // 요청 실행
  executeRequest('normal');
  
  // 요청 간 지연
  sleep(randomIntBetween(2, 5) / 10); // 0.2 ~ 0.5초 지연
}

// 트래픽 스파이크 시뮬레이션
export function spikeTraffic() {
  // 인기 카테고리에 집중
  const category = popularCategories[randomIntBetween(0, 2)]; // 상위 3개 카테고리에 집중
  const sort = sortOptions[0]; // 인기도 정렬에 집중
  
  // 추가 필터링 파라미터 (다양한 조합 생성)
  const additionalParams = {};
  if (Math.random() > 0.7) additionalParams.inStock = true;
  if (Math.random() > 0.8) additionalParams.onSale = true;
  
  // 쿼리 파라미터 및 URL 생성
  const queryParams = buildQueryParams(category, sort, additionalParams);
  const url = `${BASE_URL}/items/rank?${queryParams}`;
  
  // 요청 실행
  const result = executeRequest('spike');
  
  // 스파이크 상황에서는 재시도 로직 추가
  if (!result.success && Math.random() > 0.5) {
    // 50% 확률로 실패한 요청 재시도
    sleep(0.5); // 0.5초 대기 후 재시도
    executeRequest('spike_retry');
  }
  
  // 스파이크 상황에서는 더 짧은 지연
  sleep(randomIntBetween(1, 2) / 10); // 0.1 ~ 0.2초 지연
}

// 복구 단계 모니터링
export function recoveryTraffic() {
  // 정상 트래픽과 동일한 패턴으로 요청하여 복구 상태 확인
  const category = popularCategories[randomIntBetween(0, popularCategories.length - 1)];
  const sort = sortOptions[randomIntBetween(0, sortOptions.length - 1)];
  
  // 쿼리 파라미터 및 URL 생성
  const queryParams = buildQueryParams(category, sort);
  const url = `${BASE_URL}/items/rank?${queryParams}`;
  
  // 요청 실행 (복구 단계 태그)
  executeRequest('recovery');
  
  // 요청 간 지연
  sleep(randomIntBetween(2, 5) / 10); // 0.2 ~ 0.5초 지연
}

// 데이터 초기화 함수
export function resetData() {
  console.log('테스트 종료 전 데이터 초기화');
  const resetResponse = http.del(ORDER_ITEM_RANK_URL);
  
  check(resetResponse, {
    '초기화 API 응답 성공 (200)': (r) => r.status === 200
  });
}

// 기본 테스트 함수
export default function() {
  // 메인 함수는 normalTraffic을 실행 - 다른 단계는 별도 함수로 지정
  normalTraffic();
}

// 테스트 완료 후 분석
export function teardown(data) {
  // 테스트 소요 시간 계산
  const testDuration = (new Date().getTime() - data.testStartTime) / 1000;
  
  // 기준 응답 시간 통계 계산
  let baselineStats = {
    avg: 0,
    min: 0,
    max: 0
  };
  
  if (baselineResponseTimes.length > 0) {
    const sum = baselineResponseTimes.reduce((a, b) => a + b, 0);
    baselineStats.avg = sum / baselineResponseTimes.length;
    baselineStats.min = Math.min(...baselineResponseTimes);
    baselineStats.max = Math.max(...baselineResponseTimes);
  }
  
  console.log(`
====== 스파이크 테스트 완료 ======
테스트 시간: ${testDuration.toFixed(1)}초
정상 단계 요청: ${requestsPerPhase.values['phase_normal'] || 0}건
스파이크 단계 요청: ${requestsPerPhase.values['phase_spike'] || 0}건
복구 단계 요청: ${requestsPerPhase.values['phase_recovery'] || 0}건
기준 응답 시간 (평균/최소/최대): ${baselineStats.avg.toFixed(2)}/${baselineStats.min.toFixed(2)}/${baselineStats.max.toFixed(2)}ms
최고 응답 시간 (p99): ${peakResponseTime.values && peakResponseTime.values.p(99) ? peakResponseTime.values.p(99).toFixed(2) : 'N/A'}ms
에러율 (스파이크 중): ${errorRateUnderLoad.values && errorRateUnderLoad.values.rate ? (errorRateUnderLoad.values.rate * 100).toFixed(2) : 'N/A'}%
복구 시간 (평균): ${recoveryTime.values && recoveryTime.values.avg ? recoveryTime.values.avg.toFixed(2) : '측정불가'}ms
순위 일관성: ${rankConsistency.values && rankConsistency.values.rate ? (rankConsistency.values.rate * 100).toFixed(2) : 'N/A'}%
===========================
`);
} 