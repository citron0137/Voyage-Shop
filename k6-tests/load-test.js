import http from 'k6/http';
import { sleep, check } from 'k6';
import { Counter } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Counter('error_rate');

export const options = {
  stages: [
    { duration: '10s', target: 10 },  // 1분 동안 10명의 가상 사용자로 증가
    //{ duration: '3m', target: 10 },  // 3분 동안 10명의 가상 사용자 유지
    //{ duration: '1m', target: 0 },   // 1분 동안 0명으로 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이내에 완료되어야 함
    error_rate: ['rate<0.1'],          // 에러율이 10% 미만이어야 함
  },
};

export default function () {
  // API 엔드포인트 호출
  const res = http.get(`http://${__ENV.API_HOST}/api/v1/products`);
  
  // 응답 검증
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  // 에러 카운트
  if (res.status !== 200) {
    errorRate.add(1);
  }

  sleep(1);
} 