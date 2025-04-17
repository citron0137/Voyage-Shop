import http from 'k6/http';
import { sleep, check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// 커스텀 메트릭 정의
const productRequestsCounter = new Counter('product_requests');
const orderItemRankRequestsCounter = new Counter('order_item_rank_requests');
const errorRate = new Rate('error_rate');

// 기본 테스트 설정
export const options = {
  // 단계별 부하 증가
  stages: [
    { duration: '30s', target: 10 }, // 워밍업: 30초 동안 10명으로 증가
    { duration: '1m', target: 20 },  // 부하 증가: 1분 동안 20명으로 증가
    { duration: '2m', target: 20 },  // 유지: 2분 동안 20명 유지
    { duration: '1m', target: 50 },  // 스파이크: 1분 동안 50명으로 급증
    { duration: '3m', target: 20 },  // 회복: 3분 동안 20명으로 감소
    { duration: '30s', target: 0 },  // 종료: 30초 동안 0명으로 감소
  ],
  // 성능 임계값 설정
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 미만
    'http_req_duration{endpoint:orderItemRank}': ['p(95)<300'], // 인기 상품 API는 더 엄격한 임계값
    http_req_failed: ['rate<0.01'],   // 실패율 1% 미만
    'product_requests': ['count>100'], // 최소 100회 이상의 상품 요청
    'error_rate': ['rate<0.05'],      // 총 오류율 5% 미만
  },
};

// 헤더 설정
const headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// 메인 함수 - 가상 사용자당 실행됨
export default function() {
  // 0~4 사이의 랜덤 값으로 사용자 ID 설정
  const userId = Math.floor(Math.random() * 5) + 1;
  
  // 인기 상품 순위 조회 (높은 읽기 부하 예상 지점)
  const orderItemRankResponse = http.get(
    'http://localhost:8080/api/v1/order-item-rank',
    { headers, tags: { endpoint: 'orderItemRank' } }
  );
  
  // 메트릭 기록
  orderItemRankRequestsCounter.add(1);
  errorRate.add(orderItemRankResponse.status !== 200);
  
  // 응답 검증
  check(orderItemRankResponse, {
    'order-item-rank status is 200': (r) => r.status === 200,
    'has order-item-rank data': (r) => {
      const body = r.json();
      return body && body.success && body.data && body.data.length > 0;
    },
  });
  
  // 인기 상품에 대한 상세 조회 (임의로 첫 번째 상품 선택)
  let popularProductId;
  try {
    const rankData = orderItemRankResponse.json();
    if (rankData && rankData.data && rankData.data.length > 0) {
      popularProductId = rankData.data[0].productId;
    } else {
      popularProductId = 1; // 기본값
    }
  } catch (e) {
    popularProductId = 1; // JSON 파싱 오류 시 기본값
  }
  
  // 인기 상품 정보 조회 
  const popularProductResponse = http.get(
    `http://localhost:8080/api/v1/products/${popularProductId}`,
    { headers, tags: { endpoint: 'productDetail' } }
  );
  
  productRequestsCounter.add(1);
  errorRate.add(popularProductResponse.status !== 200);
  
  check(popularProductResponse, {
    'popular product details status is 200': (r) => r.status === 200,
  });
  
  sleep(Math.random() * 2 + 1); // 1-3초 랜덤 대기
  
  // 일반 상품 목록 조회
  const productsResponse = http.get(
    'http://localhost:8080/api/v1/products',
    { headers, tags: { endpoint: 'productsList' } }
  );
  
  productRequestsCounter.add(1);
  errorRate.add(productsResponse.status !== 200);
  
  check(productsResponse, {
    'products list status is 200': (r) => r.status === 200,
  });
  
  sleep(Math.random() + 0.5); // 0.5-1.5초 랜덤 대기
  
  // 사용자 포인트 조회
  const pointsResponse = http.get(
    `http://localhost:8080/api/v1/users/${userId}/points`,
    { headers, tags: { endpoint: 'userPoints' } }
  );
  
  check(pointsResponse, {
    'points request succeeded': (r) => r.status === 200,
  });
  
  sleep(Math.random() + 0.5); // 0.5-1.5초 랜덤 대기
  
  // 쿠폰 이벤트 목록 조회
  const couponEventsResponse = http.get(
    'http://localhost:8080/api/v1/coupon-events',
    { headers, tags: { endpoint: 'couponEvents' } }
  );
  
  check(couponEventsResponse, {
    'coupon events request succeeded': (r) => r.status === 200,
  });
  
  // 10%의 확률로 포인트 충전 요청 (쓰기 작업 테스트)
  if (Math.random() < 0.1) {
    const amount = Math.floor(Math.random() * 10000) + 1000; // 1000-11000 포인트
    const chargeResponse = http.post(
      `http://localhost:8080/api/v1/users/${userId}/points/charge`,
      JSON.stringify({ amount }),
      { headers, tags: { endpoint: 'chargePoints' } }
    );
    
    check(chargeResponse, {
      'point charge succeeded': (r) => r.status === 200,
    });
  }
  
  sleep(Math.random() * 3 + 2); // 2-5초 랜덤 대기 (페이지 탐색 시뮬레이션)
} 