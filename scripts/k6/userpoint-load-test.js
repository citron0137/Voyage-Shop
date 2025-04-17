import http from 'k6/http';
import { sleep, check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// 커스텀 메트릭 정의
const pointsOperationCounter = new Counter('points_operations');
const concurrentWritesCounter = new Counter('concurrent_writes');
const errorRate = new Rate('error_rate');

// 기본 테스트 설정
export const options = {
  // 동시성 테스트에 최적화된 단계별 부하 증가
  stages: [
    { duration: '30s', target: 5 },    // 워밍업: 30초 동안 5명으로 증가
    { duration: '30s', target: 15 },   // 부하 증가: 30초 동안 15명으로 증가
    { duration: '1m', target: 15 },    // 유지: 1분 동안 15명 유지
    { duration: '20s', target: 30 },   // 첫번째 스파이크: 20초 동안 30명으로 급증
    { duration: '40s', target: 15 },   // 회복: 40초 동안 15명으로 감소
    { duration: '20s', target: 50 },   // 두번째 스파이크: 20초 동안 50명으로 급증
    { duration: '1m', target: 15 },    // 회복: 1분 동안 15명으로 감소
    { duration: '20s', target: 0 },    // 종료: 20초 동안 0명으로 감소
  ],
  // 성능 임계값 설정
  thresholds: {
    http_req_duration: ['p(95)<1000'],                  // 95%의 요청이 1000ms 미만
    'http_req_duration{operation:charge}': ['p(95)<800'], // 포인트 충전 요청 임계값
    'http_req_duration{operation:use}': ['p(95)<800'],    // 포인트 사용 요청 임계값
    http_req_failed: ['rate<0.05'],                     // 실패율 5% 미만
  },
};

// 헤더 설정
const headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// 메인 함수 - 가상 사용자당 실행됨
export default function() {
  // 동일 사용자에 대한 동시성 테스트를 위해 1~3 사이의 고정된 값 사용
  const userId = Math.floor(Math.random() * 3) + 1;
  
  // 포인트 잔액 조회
  const pointsResponse = http.get(
    `http://localhost:8080/api/v1/users/${userId}/points`,
    { headers, tags: { operation: 'balance' } }
  );
  
  check(pointsResponse, {
    'points balance check succeeded': (r) => r.status === 200,
  });
  
  let balance = 0;
  try {
    const pointsData = pointsResponse.json();
    if (pointsData && pointsData.data) {
      balance = pointsData.data.balance;
    }
  } catch (e) {
    // 오류 처리
  }
  
  sleep(Math.random() * 0.5 + 0.2); // 0.2-0.7초 랜덤 대기
  
  // 50% 확률로 포인트 충전, 50% 확률로 포인트 사용
  if (Math.random() < 0.5) {
    // 포인트 충전 요청
    const chargeAmount = Math.floor(Math.random() * 5000) + 1000; // 1000-6000 포인트
    const chargeResponse = http.post(
      `http://localhost:8080/api/v1/users/${userId}/points/charge`,
      JSON.stringify({ amount: chargeAmount }),
      { headers, tags: { operation: 'charge' } }
    );
    
    pointsOperationCounter.add(1);
    errorRate.add(chargeResponse.status !== 200);
    
    check(chargeResponse, {
      'point charge succeeded': (r) => r.status === 200,
    });
    
    // 충전 응답에서 트랜잭션 ID 추출
    let txId = null;
    try {
      const result = chargeResponse.json();
      if (result && result.data && result.data.transactionId) {
        txId = result.data.transactionId;
      }
    } catch (e) {
      // 오류 처리
    }
    
    // 트랜잭션 기록 조회 (선택적)
    if (txId) {
      const txResponse = http.get(
        `http://localhost:8080/api/v1/users/${userId}/points/transactions/${txId}`,
        { headers, tags: { operation: 'transaction' } }
      );
      
      check(txResponse, {
        'transaction fetch succeeded': (r) => r.status === 200,
      });
    }
  } else {
    // 잔액이 있는 경우만 포인트 사용 요청
    if (balance > 100) {
      const useAmount = Math.min(balance, Math.floor(Math.random() * 1000) + 100); // 최대 잔액만큼, 최소 100
      const useResponse = http.post(
        `http://localhost:8080/api/v1/users/${userId}/points/use`,
        JSON.stringify({ amount: useAmount }),
        { headers, tags: { operation: 'use' } }
      );
      
      pointsOperationCounter.add(1);
      errorRate.add(useResponse.status !== 200);
      
      check(useResponse, {
        'point use succeeded': (r) => r.status === 200,
      });
    }
  }
  
  // 10% 확률로 동시 요청 테스트 (비관적 락 테스트)
  if (Math.random() < 0.1) {
    concurrentWritesCounter.add(1);
    
    // 빠른 연속으로 2개의 요청 보내기
    const amount1 = Math.floor(Math.random() * 1000) + 500;
    const amount2 = Math.floor(Math.random() * 1000) + 500;
    
    const requests = {
      chargeReq1: {
        method: 'POST',
        url: `http://localhost:8080/api/v1/users/${userId}/points/charge`,
        body: JSON.stringify({ amount: amount1 }),
        params: { headers, tags: { operation: 'concurrent_charge' } }
      },
      chargeReq2: {
        method: 'POST',
        url: `http://localhost:8080/api/v1/users/${userId}/points/charge`,
        body: JSON.stringify({ amount: amount2 }),
        params: { headers, tags: { operation: 'concurrent_charge' } }
      }
    };
    
    const responses = http.batch(requests);
    
    check(responses.chargeReq1, {
      'concurrent charge 1 succeeded': (r) => r.status === 200,
    });
    
    check(responses.chargeReq2, {
      'concurrent charge 2 succeeded': (r) => r.status === 200,
    });
  }
  
  sleep(Math.random() * 1 + 0.5); // 0.5-1.5초 랜덤 대기
  
  // 트랜잭션 이력 조회
  const txHistoryResponse = http.get(
    `http://localhost:8080/api/v1/users/${userId}/points/transactions`,
    { headers, tags: { operation: 'history' } }
  );
  
  check(txHistoryResponse, {
    'transaction history succeeded': (r) => r.status === 200,
  });
  
  sleep(Math.random() * 2 + 1); // 1-3초 랜덤 대기
} 