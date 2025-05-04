import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 테스트 설정 및 환경 변수
const API_HOST = __ENV.API_HOST || 'localhost:8080';
const BASE_URL = `http://${API_HOST}/api/v1`;
const ORDER_ITEM_RANK_URL = `${BASE_URL}/order-item-rank`;

// 커스텀 메트릭 정의
const rankApiCalls = new Trend('rank_api_calls');
const rankApiConsistency = new Rate('rank_api_consistency');

// 필터링 및 정렬 옵션 정의
const filterOptions = [
  { category: 'electronics' },
  { category: 'clothing' },
  { category: 'home' },
  { category: 'beauty' },
  { category: 'sports' },
  { priceMin: 1000, priceMax: 5000 },
  { priceMin: 5000, priceMax: 10000 },
  { priceMin: 10000, priceMax: 50000 },
  { inStock: true },
  { onSale: true },
  { rating: 4 }
];

const sortOptions = [
  { field: 'price', direction: 'asc' },
  { field: 'price', direction: 'desc' },
  { field: 'popularity', direction: 'desc' },
  { field: 'rating', direction: 'desc' },
  { field: 'newest', direction: 'desc' }
];

// 테스트 시나리오 정의
export const options = {
  scenarios: {
    constant_request_rate: {
      executor: 'constant-arrival-rate',
      rate: 50, // 초당 요청 수
      timeUnit: '1s',
      duration: '2m', // 테스트 지속 시간
      preAllocatedVUs: 100, // 사전 할당 가상 사용자
      maxVUs: 200, // 최대 가상 사용자
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<250'], // 95%의 요청이 250ms 이내 완료
    http_req_failed: ['rate<0.01'], // 오류율 1% 미만
    'rank_api_consistency': ['rate>0.99'], // 순위 일관성 99% 이상
  },
};

// 랜덤 필터 조합 생성
function getRandomFilters() {
  const numFilters = randomIntBetween(0, 3);
  const selectedFilters = {};
  
  const usedIndices = new Set();
  for (let i = 0; i < numFilters; i++) {
    let index;
    do {
      index = randomIntBetween(0, filterOptions.length - 1);
    } while (usedIndices.has(index));
    
    usedIndices.add(index);
    const filter = filterOptions[index];
    Object.assign(selectedFilters, filter);
  }
  
  return selectedFilters;
}

// 랜덤 정렬 옵션 선택
function getRandomSort() {
  const index = randomIntBetween(0, sortOptions.length - 1);
  return sortOptions[index];
}

// 쿼리 파라미터 생성
function buildQueryParams(filters, sort) {
  const params = new URLSearchParams();
  
  // 필터 추가
  Object.entries(filters).forEach(([key, value]) => {
    params.append(key, value);
  });
  
  // 정렬 추가
  if (sort) {
    params.append('sortBy', sort.field);
    params.append('sortDirection', sort.direction);
  }
  
  // 페이지네이션
  params.append('page', 1);
  params.append('size', 20);
  
  return params.toString();
}

// 이전 응답 결과 저장용 변수
let previousResults = null;
let requestCount = 0;

// 메인 테스트 함수
export default function() {
  requestCount++;
  
  // API 호출
  const response = http.get(ORDER_ITEM_RANK_URL, {
    tags: { name: 'OrderItemRankAPI' }
  });
  
  // 응답 검증
  const success = check(response, {
    'API 응답 성공 (200)': (r) => r.status === 200,
    '응답에 데이터 포함': (r) => {
      try {
        const data = JSON.parse(r.body);
        return data && data.data && Array.isArray(data.data) && data.data.length > 0;
      } catch (e) {
        return false;
      }
    },
  });
  
  // 응답 시간 기록
  rankApiCalls.add(response.timings.duration);
  
  // 일관성 검증 (이전 결과와 현재 결과 비교)
  if (success) {
    try {
      const responseData = JSON.parse(response.body);
      
      if (previousResults !== null) {
        // 순위 상품 ID 목록 생성
        const currentItemIds = responseData.data.map(item => item.itemId).join(',');
        const previousItemIds = previousResults.map(item => item.itemId).join(',');
        
        // 결과 일관성 확인 (매 10번째 요청마다 검증)
        // 캐싱이나 데이터 업데이트로 인해 결과가 변경될 수 있으므로 주기적으로 확인
        if (requestCount % 10 === 0) {
          const isConsistent = currentItemIds === previousItemIds;
          rankApiConsistency.add(isConsistent);
          
          if (!isConsistent) {
            console.log(`일관성 변화 감지: 상품 목록이 변경되었습니다.`);
            // 새로운 결과로 업데이트
            previousResults = responseData.data;
          }
        }
      } else {
        // 첫 결과 저장
        previousResults = responseData.data;
        rankApiConsistency.add(true); // 첫 요청은 항상 일관성 있다고 간주
      }
    } catch (e) {
      console.error('응답 파싱 오류:', e);
    }
  }
  
  // 주기적으로 초기화 API 호출 (매 200번째 요청마다)
  if (requestCount % 200 === 0) {
    console.log('순위 데이터 초기화 요청');
    const resetResponse = http.del(ORDER_ITEM_RANK_URL);
    
    check(resetResponse, {
      '초기화 API 응답 성공 (200)': (r) => r.status === 200
    });
    
    // 초기화 후 이전 결과 리셋
    previousResults = null;
  }
  
  // 요청 간 짧은 지연
  sleep(randomIntBetween(1, 3) / 10); // 0.1 ~ 0.3초 지연
} 