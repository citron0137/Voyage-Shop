import http from 'k6/http';
import { sleep, check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// 커스텀 메트릭 정의
const stockOperationCounter = new Counter('stock_operations');
const concurrentStockCounter = new Counter('concurrent_stock_ops');
const errorRate = new Rate('error_rate');

// 기본 테스트 설정
export const options = {
  // 재고 동시성 테스트를 위한 부하 설정
  stages: [
    { duration: '20s', target: 5 },    // 워밍업: 20초 동안 5명으로 증가
    { duration: '30s', target: 20 },   // 부하 증가: 30초 동안 20명으로 증가
    { duration: '1m', target: 20 },    // 유지: 1분 동안 20명 유지
    { duration: '30s', target: 40 },   // 스파이크: 30초 동안 40명으로 급증
    { duration: '1m', target: 10 },    // 회복: 1분 동안 10명으로 감소
    { duration: '20s', target: 0 },    // 종료: 20초 동안 0명으로 감소
  ],
  // 성능 임계값 설정
  thresholds: {
    http_req_duration: ['p(95)<800'],                  // 95%의 요청이 800ms 미만
    'http_req_duration{operation:decrease}': ['p(95)<600'], // 재고 감소 임계값
    http_req_failed: ['rate<0.10'],                    // 실패율 10% 미만 (재고 부족으로 인한 실패 허용)
    'error_rate': ['rate<0.20'],                       // 총 오류율 20% 미만
  },
};

// 헤더 설정
const headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// 메인 함수 - 가상 사용자당 실행됨
export default function() {
  // 인기 상품 순위 조회로 시작
  const orderItemRankResponse = http.get(
    'http://localhost:8080/api/v1/order-item-rank',
    { headers, tags: { operation: 'rank' } }
  );
  
  // 테스트할 상품 ID 결정 (인기 상품 또는 기본값)
  let productIds = [1, 2, 3]; // 기본 상품 ID 목록
  try {
    const rankData = orderItemRankResponse.json();
    if (rankData && rankData.data && rankData.data.length > 0) {
      // API 응답에서 상위 3개 상품 ID 추출
      productIds = rankData.data
        .slice(0, Math.min(3, rankData.data.length))
        .map(item => item.productId);
    }
  } catch (e) {
    // JSON 파싱 오류시 기본값 사용
  }
  
  // 테스트할 상품 ID 랜덤 선택
  const productId = productIds[Math.floor(Math.random() * productIds.length)];
  
  // 상품 정보 조회
  const productResponse = http.get(
    `http://localhost:8080/api/v1/products/${productId}`,
    { headers, tags: { operation: 'product_detail' } }
  );
  
  check(productResponse, {
    'product detail succeeded': (r) => r.status === 200,
  });
  
  // 상품 재고 정보 추출
  let currentStock = 0;
  try {
    const productData = productResponse.json();
    if (productData && productData.data) {
      currentStock = productData.data.stock;
    }
  } catch (e) {
    // 오류 처리
  }
  
  sleep(Math.random() * 0.5 + 0.3); // 0.3-0.8초 랜덤 대기
  
  // 70% 확률로 재고 감소, 30% 확률로 재고 증가
  if (Math.random() < 0.7) {
    // 재고 감소 요청
    const decreaseAmount = Math.min(currentStock, Math.floor(Math.random() * 3) + 1); // 최대 현재 재고까지, 최소 1
    
    if (decreaseAmount > 0) {
      const decreaseResponse = http.post(
        `http://localhost:8080/api/v1/products/${productId}/stock/decrease`,
        JSON.stringify({ amount: decreaseAmount }),
        { headers, tags: { operation: 'decrease' } }
      );
      
      stockOperationCounter.add(1);
      errorRate.add(decreaseResponse.status !== 200);
      
      check(decreaseResponse, {
        'stock decrease succeeded': (r) => r.status === 200,
      });
    }
  } else {
    // 재고 증가 요청
    const increaseAmount = Math.floor(Math.random() * 5) + 1; // 1-5 랜덤 증가
    
    const increaseResponse = http.post(
      `http://localhost:8080/api/v1/products/${productId}/stock/increase`,
      JSON.stringify({ amount: increaseAmount }),
      { headers, tags: { operation: 'increase' } }
    );
    
    stockOperationCounter.add(1);
    errorRate.add(increaseResponse.status !== 200);
    
    check(increaseResponse, {
      'stock increase succeeded': (r) => r.status === 200,
    });
  }
  
  sleep(Math.random() * 0.5 + 0.2); // 0.2-0.7초 랜덤 대기
  
  // 15% 확률로 동시성 테스트 (비관적 락 테스트)
  if (Math.random() < 0.15) {
    concurrentStockCounter.add(1);
    
    // 동일 상품에 대해 동시에 여러 요청 보내기
    const amount1 = Math.floor(Math.random() * 2) + 1; // 1-2 랜덤 감소
    const amount2 = Math.floor(Math.random() * 2) + 1; // 1-2 랜덤 감소
    const amount3 = Math.floor(Math.random() * 3) + 1; // 1-3 랜덤 증가
    
    const requests = {
      decreaseReq1: {
        method: 'POST',
        url: `http://localhost:8080/api/v1/products/${productId}/stock/decrease`,
        body: JSON.stringify({ amount: amount1 }),
        params: { headers, tags: { operation: 'concurrent_decrease' } }
      },
      decreaseReq2: {
        method: 'POST',
        url: `http://localhost:8080/api/v1/products/${productId}/stock/decrease`,
        body: JSON.stringify({ amount: amount2 }),
        params: { headers, tags: { operation: 'concurrent_decrease' } }
      },
      increaseReq: {
        method: 'POST',
        url: `http://localhost:8080/api/v1/products/${productId}/stock/increase`,
        body: JSON.stringify({ amount: amount3 }),
        params: { headers, tags: { operation: 'concurrent_increase' } }
      }
    };
    
    const responses = http.batch(requests);
    
    // 동시 요청 결과 확인
    ['decreaseReq1', 'decreaseReq2', 'increaseReq'].forEach(req => {
      check(responses[req], {
        [`concurrent ${req} status is 200 or 400`]: (r) => r.status === 200 || r.status === 400,
      });
    });
  }
  
  sleep(Math.random() * 1 + 0.5); // 0.5-1.5초 랜덤 대기
  
  // 업데이트 후 상품 정보 다시 조회
  const updatedProductResponse = http.get(
    `http://localhost:8080/api/v1/products/${productId}`,
    { headers, tags: { operation: 'product_detail_after' } }
  );
  
  check(updatedProductResponse, {
    'updated product detail succeeded': (r) => r.status === 200,
  });
  
  sleep(Math.random() * 2 + 1); // 1-3초 랜덤 대기
} 