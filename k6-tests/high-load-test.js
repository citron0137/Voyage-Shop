import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 }, // 30초 동안 50개의 VU로 증가
    { duration: '1m', target: 50 },  // 1분 동안 50개의 VU 유지
    { duration: '30s', target: 0 },  // 30초 동안 0개의 VU로 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이내에 완료되어야 함
    'http_req_failed': ['rate<0.1'],  // 실패율이 10% 미만이어야 함
  },
};

export default function () {
  const res = http.get(`http://${__ENV.API_HOST}/api/products`);
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
} 