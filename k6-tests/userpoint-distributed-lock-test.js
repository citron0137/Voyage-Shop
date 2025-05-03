import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';
import { randomString, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// ----------------------- 테스트 설정 -----------------------

// 테스트 기본 설정
const API_HOST = __ENV.API_HOST || 'localhost:8080';
const TEST_USER_COUNT = parseInt(__ENV.TEST_USER_COUNT || '20'); // 테스트에 사용할 사용자 수
const RUN_TEST = __ENV.RUN_TEST === 'true'; // 테스트 실행 여부 플래그

// 사용자 지정 테스트 파라미터
const LOAD_TEST_DURATION = __ENV.LOAD_TEST_DURATION || '2m';
const LOAD_TEST_RATE = parseInt(__ENV.LOAD_TEST_RATE || '50');
const INITIAL_POINT = parseInt(__ENV.INITIAL_POINT || '100');

console.log(`테스트 설정:
- API 호스트: ${API_HOST}
- 테스트 사용자 수: ${TEST_USER_COUNT}
- 테스트 실행 여부: ${RUN_TEST}
- 부하 테스트 지속 시간: ${LOAD_TEST_DURATION}
- 초당 요청 수: ${LOAD_TEST_RATE}
- 초기 포인트: ${INITIAL_POINT}
`);

// API 엔드포인트
const ENDPOINTS = {
  CREATE_USER: `http://${API_HOST}/api/v1/users`,
  USER_POINT: (userId) => `http://${API_HOST}/api/v1/user-points/${userId}`,
  CHARGE_POINT: (userId) => `http://${API_HOST}/api/v1/user-points/${userId}/charge`
};

// HTTP 요청 공통 헤더
const HEADERS = {
  'Content-Type': 'application/json',
};

// ----------------------- 메트릭 정의 -----------------------

// 테스트 결과 측정을 위한 커스텀 메트릭
const METRICS = {
  errors: new Counter('point_charge_errors'),     // 포인트 충전 오류 수
  time: new Trend('point_charge_time'),           // 포인트 충전 소요 시간
  success: new Rate('point_charge_success'),      // 포인트 충전 성공률
  consistency: new Counter('point_consistency_failures') // 포인트 데이터 일관성 문제
};

// ----------------------- 테스트 시나리오 설정 -----------------------

export const options = {
  scenarios: {
    // 다수 사용자 동시 요청 테스트
    distributed_lock_test: {
      executor: 'constant-arrival-rate',
      rate: LOAD_TEST_RATE,  // 초당 요청 수 (환경 변수로 설정 가능)
      timeUnit: '1s',        // 시간 단위
      duration: LOAD_TEST_DURATION, // 테스트 지속 시간 (환경 변수로 설정 가능)
      preAllocatedVUs: 100,  // 미리 할당할 VU(가상 사용자) 수
      maxVUs: 200,           // 최대 VU 수
      exec: 'distributedTest', // 실행할 함수 지정
    },
    
    // 단일 사용자 집중 테스트 (동시성 이슈 확인)
    single_user_spike: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 100,
      stages: [
        { duration: '20s', target: 20 },  // 20초 동안 초당 20개 요청
        { duration: '30s', target: 50 },  // 30초 동안 초당 50개 요청
        { duration: '10s', target: 0 },   // 10초 동안 정리
      ],
      exec: 'singleUserTest', // 단일 사용자 테스트 함수 지정
    }
  },
  
  // 테스트 성공 기준
  thresholds: {
    http_req_duration: ['p(95)<500'],         // 95%의 요청이 500ms 이내 완료
    'point_charge_time': ['p(99)<800'],       // 99%의 포인트 충전이 800ms 이내 완료
    'point_charge_errors': ['count<50'],      // 오류 수 50개 미만
    'point_charge_success': ['rate>0.95'],    // 성공률 95% 이상
    'point_consistency_failures': ['count<5'] // 데이터 일관성 오류 5건 미만
  },
};

// ----------------------- 사용자 관리 함수 -----------------------

/**
 * 테스트용 사용자를 생성합니다.
 * @returns {string|null} 생성된 사용자 ID 또는 실패 시 null
 */
function createTestUser() {
  console.log("사용자 생성")
  const response = http.post(ENDPOINTS.CREATE_USER, { headers: HEADERS });
  
  if (response.json().success) {
    const userData = response.json();
    return userData.data.id;
  } else {
    console.error(`사용자 생성 실패: ${response.status}, ${response.body}`);
    return null;
  }
}

/**
 * 사용자에게 초기 포인트를 충전합니다.
 * @param {string} userId 사용자 ID
 * @param {number} amount 충전할 포인트 양
 */
function initializeUserPoint(userId, amount = INITIAL_POINT) {
  http.post(
    ENDPOINTS.CHARGE_POINT(userId),
    JSON.stringify({ amount }),
    { headers: HEADERS }
  );
}

// ----------------------- 테스트 준비 함수 -----------------------

/**
 * 테스트 시작 전 환경을 준비합니다.
 * @returns {Object} 테스트에 사용할 데이터
 */
export function setup() {
  console.log(`${TEST_USER_COUNT}명의 테스트 사용자를 생성합니다...`);
  
  // 일반 테스트용 사용자들 생성
  const testUsers = [];
  for (let i = 0; i < TEST_USER_COUNT; i++) {
    const userId = createTestUser();
    if (userId) {
      testUsers.push(userId);
      initializeUserPoint(userId, INITIAL_POINT);
    }
  }
  
  console.log(`생성된 테스트 사용자 수: ${testUsers.length}`);
  
  // 단일 사용자 집중 테스트용 사용자 생성
  const singleTestUserId = createTestUser();
  console.log(`단일 사용자 테스트 ID: ${singleTestUserId}`);
  
  // 사용자에게 RUN_TEST가 설정되지 않았다면 안내 메시지 출력
  if (!RUN_TEST) {
    console.log("\n테스트 데이터가 준비되었습니다.");
    console.log("실제 테스트를 실행하려면 다음 명령어를 사용하세요:");
    console.log(`k6 run -e RUN_TEST=true -e API_HOST=${API_HOST} k6-tests/userpoint-distributed-lock-test.js`);
    console.log("\n추가 옵션:");
    console.log("- TEST_USER_COUNT: 테스트할 사용자 수");
    console.log("- LOAD_TEST_DURATION: 부하 테스트 지속 시간 (예: 2m, 30s)");
    console.log("- LOAD_TEST_RATE: 초당 요청 수");
    console.log("- INITIAL_POINT: 사용자당 초기 포인트");
    console.log("\n예시:");
    console.log(`k6 run -e RUN_TEST=true -e API_HOST=${API_HOST} -e LOAD_TEST_RATE=100 -e LOAD_TEST_DURATION=1m k6-tests/userpoint-distributed-lock-test.js`);
  }
  
  return { 
    testUsers: testUsers,
    singleTestUserId: singleTestUserId,
    runTest: RUN_TEST
  };
}

// ----------------------- 테스트 실행 함수 -----------------------

/**
 * 포인트 충전 테스트를 실행합니다.
 * @param {string} userId 테스트할 사용자 ID
 */
function runPointChargeTest(userId) {
  // 100~999 사이의 랜덤 충전 금액
  const chargeAmount = randomIntBetween(100, 999);
  
  // 진정한 동시성 테스트를 위한 짧은 랜덤 지연
  sleep(Math.random() * 0.05); 
  
  const payload = JSON.stringify({ amount: chargeAmount });
  const requestOptions = {
    headers: HEADERS,
    tags: { name: 'PointChargeRequest' }
  };
  
  // 요청 시간 측정 시작
  const startTime = new Date().getTime();
  const response = http.post(ENDPOINTS.CHARGE_POINT(userId), payload, requestOptions);
  const duration = new Date().getTime() - startTime;
  
  // 메트릭에 응답 시간 기록
  METRICS.time.add(duration);
  
  // 응답 검증
  const success = check(response, {
    '포인트 충전 성공 (status: true)': (r) => r.json().success === true,
    '응답에 데이터 포함': (r) => {
      try {
        const data = JSON.parse(r.body);
        return data.data && data.data.amount !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
  
  // 성공/실패 기록
  METRICS.success.add(success);
  
  if (!success) {
    METRICS.errors.add(1);
    console.error(`포인트 충전 실패 - 사용자: ${userId}, 오류 코드: ${response.json().error?.code}`);
  } else {
    // 성공한 경우만 일관성 검증
    verifyUserPointConsistency(userId, response);
  }
  
  // 요청 간 간격 추가
  sleep(Math.random());
}

/**
 * 사용자 포인트 데이터의 일관성을 검증합니다.
 * @param {string} userId 사용자 ID
 * @param {Object} chargeResponse 충전 요청 응답
 */
function verifyUserPointConsistency(userId, chargeResponse) {
  try {
    // 충전 응답에서 포인트 금액 추출
    const chargeData = JSON.parse(chargeResponse.body);
    const chargedAmount = chargeData.data.amount;
    
    // 현재 포인트 조회
    const getUserResponse = http.get(ENDPOINTS.USER_POINT(userId));
    
    if (getUserResponse.status === 200) {
      const userData = JSON.parse(getUserResponse.body);
      
      // 포인트 일관성 검증
      if (userData.data && userData.data.amount !== chargedAmount) {
        console.error(`포인트 일관성 오류 - 사용자: ${userId}, 충전 응답: ${chargedAmount}, 조회 결과: ${userData.data.amount}`);
        METRICS.consistency.add(1);
      }
    }
  } catch (e) {
    console.error(`일관성 검증 오류: ${e.message}`);
  }
}

// ----------------------- 테스트 시나리오 함수 -----------------------

/**
 * 메인 테스트 함수 - 테스트 실행 여부를 확인하고 실행합니다.
 * @param {Object} data setup 함수에서 반환된 데이터
 */
export default function (data) {
  // RUN_TEST 환경변수가 true가 아니면 테스트 실행하지 않음
  if (!data.runTest) {
    return;
  }
  
  // 기본 분산 테스트 실행
  distributedTest(data);
}

/**
 * 일반 테스트 시나리오: 여러 사용자에 대한 랜덤 포인트 충전
 * @param {Object} data setup 함수에서 반환된 데이터
 */
export function distributedTest(data) {
  if (!data.testUsers || data.testUsers.length === 0) {
    console.error('사용 가능한 테스트 사용자가 없습니다.');
    return;
  }
  
  // 랜덤 사용자 선택
  const userIndex = randomIntBetween(0, data.testUsers.length - 1);
  const userId = data.testUsers[userIndex];
  
  runPointChargeTest(userId);
}

/**
 * 단일 사용자 집중 테스트 (동시성 이슈 확인용)
 * @param {Object} data setup 함수에서 반환된 데이터
 */
export function singleUserTest(data) {
  // RUN_TEST 환경변수가 true가 아니면 테스트 실행하지 않음
  if (!data.runTest) {
    return;
  }
  
  if (!data.singleTestUserId) {
    console.error('단일 테스트 사용자가 생성되지 않았습니다.');
    return;
  }
  
  runPointChargeTest(data.singleTestUserId);
} 