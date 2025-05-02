import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 커스텀 메트릭 정의
const PointChargeError = new Counter('point_charge_errors');
const PointChargeTime = new Trend('point_charge_time');
const PointChargeSuccess = new Rate('point_charge_success');
const PointConsistency = new Counter('point_consistency_failures');

export const options = {
  scenarios: {
    // 다수의 사용자가 동시에 다양한 포인트 충전을 시도하는 테스트
    distributed_lock_test: {
      executor: 'constant-arrival-rate',
      rate: 50,             // 초당 요청 수
      timeUnit: '1s',       // 시간 단위
      duration: '2m',       // 테스트 지속 시간
      preAllocatedVUs: 100, // 미리 할당할 VU(가상 사용자) 수
      maxVUs: 200,          // 최대 VU 수
    },
    
    // 단일 사용자에 대한 집중 테스트 (동시성 이슈를 더 명확히 확인)
    single_user_spike: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 100,
      stages: [
        { duration: '20s', target: 20 },  // 20초 동안 초당 20개 요청으로 증가
        { duration: '30s', target: 50 },  // 30초 동안 초당 50개 요청으로 급증
        { duration: '10s', target: 0 },   // 10초 동안 0개로 감소
      ],
      exec: 'singleUserTest', // 특별히 단일 사용자 테스트를 위한 함수
    }
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],         // 95%의 요청이 500ms 이내에 완료
    'point_charge_time': ['p(99)<800'],       // 99%의 포인트 충전이 800ms 이내에 완료
    'point_charge_errors': ['count<50'],      // 전체 테스트 동안 오류 수 50개 미만
    'point_charge_success': ['rate>0.95'],    // 성공률 95% 이상
    'point_consistency_failures': ['count<5'] // 포인트 일관성 오류 5건 미만
  },
};

// 테스트 시작 전 준비
export function setup() {
  // 테스트 사용자 생성 또는 초기화 로직을 여기에 추가할 수 있음
  // 테스트에 사용될 특정 사용자 ID (분산 락 테스트용)
  const singleTestUserId = 'test_lock_user_' + randomString(5);
  
  console.log(`단일 사용자 테스트 ID: ${singleTestUserId}`);
  
  // 초기 포인트 설정을 위한 API 호출 등을 추가할 수 있음
  
  return { singleTestUserId };
}

// 일반 테스트 시나리오: 여러 사용자에 대한 랜덤 포인트 충전
export default function (data) {
  // 매 요청마다 다른 사용자 ID 사용 (실제 환경을 시뮬레이션)
  const userId = 'user_' + randomString(8);
  runPointChargeTest(userId);
}

// 단일 사용자 집중 테스트 (동시성 이슈 확인용)
export function singleUserTest(data) {
  // setup에서 생성한 고정 사용자 ID 사용
  const userId = data.singleTestUserId;
  runPointChargeTest(userId);
}

// 포인트 충전 테스트 실행 함수
function runPointChargeTest(userId) {
  const chargeAmount = Math.floor(Math.random() * 900) + 100; // 100~999 사이의 랜덤 금액
  const chargeUrl = `http://${__ENV.API_HOST || 'localhost:8080'}/api/v1/user-points/${userId}/charge`;
  
  // 아주 짧은 랜덤 지연 (진정한 동시성 테스트를 위해)
  sleep(Math.random() * 0.05); 
  
  const payload = JSON.stringify({
    amount: chargeAmount
  });
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: { name: 'PointChargeRequest' }, // 요청 태그 (요청 구분용)
  };
  
  const startTime = new Date().getTime();
  const response = http.post(chargeUrl, payload, params);
  const endTime = new Date().getTime();
  
  // 응답 시간 기록
  PointChargeTime.add(endTime - startTime);
  
  // 응답 검증
  const success = check(response, {
    '포인트 충전 성공 (200)': (r) => r.status === 200,
    '응답에 데이터 포함': (r) => {
      try {
        const data = JSON.parse(r.body);
        return data.data && data.data.amount !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
  
  // 성공 여부 기록
  PointChargeSuccess.add(success);
  
  if (!success) {
    PointChargeError.add(1);
    console.error(`포인트 충전 실패 - 사용자: ${userId}, 상태 코드: ${response.status}`);
  }
  
  // 응답받은 후 사용자의 현재 포인트 조회 (일관성 확인)
  if (success) {
    verifyUserPointConsistency(userId, response);
  }
  
  // 요청 간 간격 추가
  sleep(Math.random());
}

// 사용자 포인트 일관성 검증 함수
function verifyUserPointConsistency(userId, chargeResponse) {
  try {
    // 충전 응답에서 포인트 금액 추출
    const chargeData = JSON.parse(chargeResponse.body);
    const chargedAmount = chargeData.data.amount;
    
    // 현재 포인트 조회 API 호출
    const getUserUrl = `http://${__ENV.API_HOST || 'localhost:8080'}/api/v1/user-points/${userId}`;
    const getUserResponse = http.get(getUserUrl);
    
    if (getUserResponse.status === 200) {
      const userData = JSON.parse(getUserResponse.body);
      
      // 조회된 포인트와 충전 응답 포인트 비교
      if (userData.data && userData.data.amount !== chargedAmount) {
        console.error(`포인트 일관성 오류 - 사용자: ${userId}, 충전 응답: ${chargedAmount}, 조회 결과: ${userData.data.amount}`);
        PointConsistency.add(1);
      }
    }
  } catch (e) {
    console.error(`일관성 검증 오류: ${e.message}`);
  }
} 