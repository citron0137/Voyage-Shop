import http from 'k6/http';
import { sleep, check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { PrometheusRemote } from 'k6/x/prometheus-rw';
import tracing from 'k6/x/tracing';
import loki from 'k6/x/loki';

// 커스텀 메트릭 정의
const productRequestsCounter = new Counter('product_requests');
const orderItemRankRequestsCounter = new Counter('order_item_rank_requests');
const errorRate = new Rate('error_rate');
const productDetailResponseTime = new Trend('product_detail_response_time');

// LGTM 통합 설정
const prom = new PrometheusRemote({
  url: 'http://prometheus:9090/api/v1/write',
  flushInterval: '5s',
});

// Loki에 로그 전송 설정
const lokiClient = new loki.Client({
  address: 'http://loki:3100/loki/api/v1/push',
  interval: '5s',
  labels: {
    app: 'k6',
    test_name: 'voyage-shop-load-test',
  },
});

// 트레이싱 설정
const tracer = new tracing.Tracer({
  endpoint: 'tempo:4317',
  service_name: 'k6-voyage-shop',
  service_version: '1.0.0',
  exporter: 'otlp',
});

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
  // 트레이싱 스팬 시작
  const rootSpan = tracer.startSpan("user_session");
  
  try {
    // 0~4 사이의 랜덤 값으로 사용자 ID 설정
    const userId = Math.floor(Math.random() * 5) + 1;
    
    // 인기 상품 순위 조회 (높은 읽기 부하 예상 지점)
    const orderItemRankSpan = tracer.startSpan("get_order_item_rank", { parent: rootSpan });
    
    // 로그 기록
    lokiClient.log(`사용자 ${userId}: 인기 상품 순위 조회 시작`);
    
    const orderItemRankResponse = http.get(
      'http://localhost:8080/api/v1/order-item-rank',
      { headers, tags: { endpoint: 'orderItemRank' } }
    );
    
    // 메트릭 기록
    orderItemRankRequestsCounter.add(1);
    errorRate.add(orderItemRankResponse.status !== 200);
    
    orderItemRankSpan.setAttribute("http.status_code", orderItemRankResponse.status);
    orderItemRankSpan.setAttribute("http.response_time", orderItemRankResponse.timings.duration);
    
    // Prometheus에 메트릭 전송
    prom.add('request_duration_seconds', orderItemRankResponse.timings.duration / 1000, {
      endpoint: 'orderItemRank',
      method: 'GET',
      status: orderItemRankResponse.status.toString(),
    });
    
    // 로그 기록
    if (orderItemRankResponse.status !== 200) {
      lokiClient.log(`사용자 ${userId}: 인기 상품 조회 실패 - 상태 코드 ${orderItemRankResponse.status}`, 'error');
    }
    
    // 응답 검증
    const checkResult = check(orderItemRankResponse, {
      'order-item-rank status is 200': (r) => r.status === 200,
      'has order-item-rank data': (r) => {
        const body = r.json();
        return body && body.success && body.data && body.data.length > 0;
      },
    });
    
    if (!checkResult) {
      orderItemRankSpan.setAttribute("error", true);
      orderItemRankSpan.addEvent("check_failed");
    }
    
    orderItemRankSpan.end();
    
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
      lokiClient.log(`사용자 ${userId}: JSON 파싱 오류 - ${e.message}`, 'error');
    }
    
    const productDetailSpan = tracer.startSpan("get_product_detail", { parent: rootSpan });
    productDetailSpan.setAttribute("product.id", popularProductId);
    
    // 인기 상품 정보 조회 
    const startTime = Date.now();
    const popularProductResponse = http.get(
      `http://localhost:8080/api/v1/products/${popularProductId}`,
      { headers, tags: { endpoint: 'productDetail' } }
    );
    const responseTime = Date.now() - startTime;
    
    productRequestsCounter.add(1);
    errorRate.add(popularProductResponse.status !== 200);
    productDetailResponseTime.add(responseTime);
    
    productDetailSpan.setAttribute("http.status_code", popularProductResponse.status);
    productDetailSpan.setAttribute("http.response_time", popularProductResponse.timings.duration);
    
    // Prometheus에 메트릭 전송
    prom.add('request_duration_seconds', popularProductResponse.timings.duration / 1000, {
      endpoint: 'productDetail',
      method: 'GET',
      product_id: popularProductId.toString(),
      status: popularProductResponse.status.toString(),
    });
    
    // 응답 검증
    const productCheckResult = check(popularProductResponse, {
      'popular product details status is 200': (r) => r.status === 200,
    });
    
    if (!productCheckResult) {
      productDetailSpan.setAttribute("error", true);
      lokiClient.log(`사용자 ${userId}: 상품 상세 조회 실패 - 상태 코드 ${popularProductResponse.status}`, 'error');
    }
    
    productDetailSpan.end();
    
    sleep(Math.random() * 2 + 1); // 1-3초 랜덤 대기
    
    // 기타 API 호출은 기존과 동일하게 유지 (간결성을 위해 트레이싱 코드 생략)
    
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
      const chargePointsSpan = tracer.startSpan("charge_points", { parent: rootSpan });
      
      const amount = Math.floor(Math.random() * 10000) + 1000; // 1000-11000 포인트
      chargePointsSpan.setAttribute("points.amount", amount);
      
      lokiClient.log(`사용자 ${userId}: ${amount} 포인트 충전 시도`);
      
      const chargeResponse = http.post(
        `http://localhost:8080/api/v1/users/${userId}/points/charge`,
        JSON.stringify({ amount }),
        { headers, tags: { endpoint: 'chargePoints' } }
      );
      
      chargePointsSpan.setAttribute("http.status_code", chargeResponse.status);
      
      const chargeCheckResult = check(chargeResponse, {
        'point charge succeeded': (r) => r.status === 200,
      });
      
      if (!chargeCheckResult) {
        chargePointsSpan.setAttribute("error", true);
        lokiClient.log(`사용자 ${userId}: 포인트 충전 실패 - 상태 코드 ${chargeResponse.status}`, 'error');
      } else {
        lokiClient.log(`사용자 ${userId}: ${amount} 포인트 충전 성공`);
      }
      
      chargePointsSpan.end();
    }
    
    sleep(Math.random() * 3 + 2); // 2-5초 랜덤 대기 (페이지 탐색 시뮬레이션)
  } finally {
    // 루트 스팬 종료
    rootSpan.end();
  }
}

// 테스트 종료 후 실행
export function teardown() {
  prom.flush();
  lokiClient.flush();
} 