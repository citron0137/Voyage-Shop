import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 커스텀 메트릭 정의
const concurrencyFailures = new Counter('concurrency_failures');
const expectedAmountMismatch = new Counter('expected_amount_mismatch');
const successRate = new Rate('success_rate');

export const options = {
  scenarios: {
    // 동일한 사용자 포인트에 대한 동시 충전 테스트
    same_user_point_charge: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 100,
      stages: [
        { duration: '30s', target: 30 },  // 30초 동안 초당 30개 요청으로 증가
        { duration: '1m', target: 50 },   // 1분 동안 초당 50개 요청으로 증가
        { duration: '30s', target: 0 },   // 30초 동안 0개로 감소
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],      // 95%의 요청이 500ms 이내에 완료
    'http_req_failed': ['rate<0.05'],      // HTTP 실패율 5% 미만
    'concurrency_failures': ['count<10'],  // 동시성 실패 수 10건 미만
    'expected_amount_mismatch': ['count<5'], // 예상 금액 불일치 5건 미만
    'success_rate': ['rate>0.95'],         // 성공률 95% 이상
  },
};

// 테스트 사용자 ID (특정 사용자에 대해 동시성 테스트)
const TEST_USER_ID = 'user123'; 
// 각 요청당 충전할 포인트 금액
const CHARGE_AMOUNT = 100;

// 테스트 시작 전 초기 포인트 확인
export function setup() {
  const url = `http://${__ENV.API_HOST || 'localhost:8080'}/api/v1/user-points/${TEST_USER_ID}`;
  const response = http.get(url);
  
  if (response.status === 200) {
    try {
      const data = JSON.parse(response.body);
      if (data.data) {
        return { initialAmount: data.data.amount };
      }
    } catch (e) {
      console.error('초기 포인트 파싱 오류:', e);
    }
  }
  
  return { initialAmount: 0 };
}

export default function (data) {
  // 동일한 사용자에 대해 포인트 충전 요청
  const chargeUrl = `http://${__ENV.API_HOST || 'localhost:8080'}/api/v1/user-points/${TEST_USER_ID}/charge`;
  
  // 0.1초 이내의 랜덤한 지연 추가 (모든 요청이 정확히 동시에 도달하지 않도록)
  sleep(Math.random() * 0.1);
  
  const payload = JSON.stringify({
    amount: CHARGE_AMOUNT
  });
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  
  const response = http.post(chargeUrl, payload, params);
  
  // 응답 검증
  const checkResult = check(response, {
    '충전 요청 성공 (200)': (r) => r.status === 200,
    '응답 본문 존재': (r) => r.body.length > 0,
  });
  
  // 성공률 기록
  successRate.add(checkResult);
  
  if (response.status === 200) {
    try {
      const responseData = JSON.parse(response.body);
      if (responseData.data) {
        // 여기서 예상되는 금액 검증 로직을 추가할 수 있음
        // 그러나 동시성 테스트에서는 정확한 예상이 어려울 수 있음
      }
    } catch (e) {
      console.error('응답 데이터 파싱 오류:', e);
      concurrencyFailures.add(1);
    }
  } else {
    concurrencyFailures.add(1);
  }
  
  // 요청 간 짧은 간격 추가
  sleep(randomIntBetween(1, 2));
}

// 테스트 종료 후 최종 포인트 확인
export function teardown(data) {
  const url = `http://${__ENV.API_HOST || 'localhost:8080'}/api/v1/user-points/${TEST_USER_ID}`;
  const response = http.get(url);
  
  if (response.status === 200) {
    try {
      const responseData = JSON.parse(response.body);
      if (responseData.data) {
        const finalAmount = responseData.data.amount;
        const initialAmount = data.initialAmount || 0;
        
        console.log(`초기 포인트: ${initialAmount}`);
        console.log(`최종 포인트: ${finalAmount}`);
        console.log(`예상 증가분: ${__ITER * CHARGE_AMOUNT}`); // __ITER은 총 반복 횟수
        
        // 실제 증가분과 예상 증가분 비교
        // 동시성 이슈가 있다면 실제 증가분이 예상보다 적을 수 있음
        if (finalAmount - initialAmount < __ITER * CHARGE_AMOUNT) {
          console.log('경고: 최종 포인트가 예상보다 적습니다! 동시성 이슈 가능성 있음');
          expectedAmountMismatch.add(1);
        }
      }
    } catch (e) {
      console.error('최종 포인트 파싱 오류:', e);
    }
  }
} 