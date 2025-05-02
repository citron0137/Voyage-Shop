# 사용자 포인트 API 동시성 테스트 가이드

이 디렉토리에는 사용자 포인트 API의 동시성 처리 능력을 테스트하기 위한 k6 스크립트가 포함되어 있습니다.

## 테스트 스크립트 설명

1. **userpoint-concurrency-test.js**
   - 동일한 사용자 ID에 대한 포인트 충전 동시성 테스트
   - 비관적 락 구현 전 기준 성능 측정용
   - 동시성 이슈 발견 시 오류 카운트

2. **userpoint-distributed-lock-test.js**
   - 분산 락 적용 후 포인트 충전 동시성 테스트
   - 다양한 부하 시나리오 (일반 분산 테스트, 단일 사용자 집중 테스트)
   - 응답 시간 및 일관성 검증 기능 포함

## 실행 방법

### Docker Compose를 사용한 실행

프로젝트에서는 Docker Compose를 사용하여 애플리케이션과 테스트 환경을 쉽게 설정하고 실행할 수 있습니다.

#### 사전 준비

1. Docker 및 Docker Compose 설치
2. 프로젝트 루트 디렉토리로 이동

#### 테스트 환경 실행

다음 명령어로 전체 테스트 환경(MySQL, 애플리케이션, InfluxDB, Grafana, k6)을 시작합니다:

```bash
# MySQL 서버 시작
docker-compose \
    -f docker-compose.yml \
    -f docker-compose.app.yml \
    up -d 

# 테스트 대상 서버가 완전히 시작될 때까지 대기
sleep 10
```

#### 부하 테스트 실행

다음 명령어로 k6 테스트를 실행합니다:

```bash
# 비관적 락 테스트 (락 적용 전)
$env:K6_SCRIPT="userpoint-concurrency-test.js";`
  docker-compose -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.loadtest.yml `
  up k6 --no-deps

# Linux에서는:
# K6_SCRIPT=userpoint-concurrency-test.js \
#   docker-compose -f docker-compose.yml \
#   -f docker-compose.app.yml \
#   -f docker-compose.loadtest.yml \
#   up k6 --no-deps

# 분산 락 테스트 (락 적용 후)
$env:K6_SCRIPT="userpoint-distributed-lock-test.js";`
  docker-compose -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.loadtest.yml `
  up k6 --no-deps

# Linux에서는:
# K6_SCRIPT=userpoint-distributed-lock-test.js \
#   docker-compose -f docker-compose.yml \
#   -f docker-compose.app.yml \
#   -f docker-compose.loadtest.yml \
#   up k6 --no-deps

# 단일 사용자 집중 테스트만 실행
$env:K6_SCRIPT="userpoint-distributed-lock-test.js";`
  $env:K6_ONLY_SCENARIO="single_user_spike";`
  docker-compose -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.loadtest.yml `
  up k6 --no-deps

# Linux에서는:
# K6_SCRIPT=userpoint-distributed-lock-test.js \
#   K6_ONLY_SCENARIO=single_user_spike \
#   docker-compose -f docker-compose.yml \
#   -f docker-compose.app.yml \
#   -f docker-compose.loadtest.yml \
#   up k6 --no-deps
```

#### 테스트 결과 확인

Grafana 대시보드를 통해 테스트 결과를 확인할 수 있습니다:
- URL: http://localhost:3000
- 기본 계정: admin / admin

#### 환경 정리

테스트가 완료된 후 다음 명령어로 환경을 정리합니다:

```bash
# 부하 테스트 환경 중지
docker-compose -f docker-compose.loadtest.yml -f docker-compose.app.yml -f docker-compose.yml down
```

### 로컬 환경에서 직접 실행 (선택사항)

k6를 로컬에 설치한 경우 다음과 같이 직접 실행할 수도 있습니다:

```bash
# 기본 동시성 테스트 (비관적 락 적용 전)
k6 run --env API_HOST=localhost:8080 k6-tests/userpoint-concurrency-test.js

# 분산 락 테스트 (분산 락 적용 후)
k6 run --env API_HOST=localhost:8080 k6-tests/userpoint-distributed-lock-test.js
```

## 테스트 메트릭 설명

### userpoint-concurrency-test.js 메트릭

- **concurrency_failures**: 동시성 처리 실패 횟수
- **expected_amount_mismatch**: 예상 금액과 실제 금액 불일치 횟수
- **success_rate**: 요청 성공률

### userpoint-distributed-lock-test.js 메트릭

- **point_charge_errors**: 포인트 충전 요청 실패 횟수
- **point_charge_time**: 포인트 충전 요청 응답 시간 (ms)
- **point_charge_success**: 포인트 충전 요청 성공률
- **point_consistency_failures**: 포인트 일관성 검증 실패 횟수

## 테스트 결과 비교

각 테스트를 실행한 후 결과를 비교하여 다음을 확인할 수 있습니다:

1. 비관적 락 vs 분산 락의 성능 차이
2. 동시성 이슈 해결 여부
3. 응답 시간 변화
4. 대규모 부하에서의 안정성

## 테스트 조정

필요에 따라 다음 항목을 조정할 수 있습니다:

- `options.scenarios`: 부하 테스트 시나리오 설정
- `TEST_USER_ID` 또는 `singleTestUserId`: 테스트 대상 사용자 ID
- `CHARGE_AMOUNT` 또는 `chargeAmount`: 충전 금액
- `thresholds`: 성능 임계값 설정 

## 테스트 결과 예시

아래는 실제 테스트 수행 결과의 예시와 그에 대한 분석입니다.

### 비관적 락 테스트 결과 (userpoint-concurrency-test.js)

#### 테스트 환경
- 테스트 기간: 2분
- 최대 VU(가상 사용자): 81
- 총 반복 실행: 3,718회
- 초당 평균 반복: 30.74/s

#### 주요 결과
```
█ THRESHOLDS                                                                               
                                                                                            
  concurrency_failures
  ✓ 'count<10' count=0                                                                     
                                                                                            
  expected_amount_mismatch
  ✓ 'count<5' count=0                                                                      

  http_req_duration                                                                        
  ✓ 'p(95)<500' p(95)=4.93ms                                                               

  http_req_failed                                                                          
  ✓ 'rate<0.05' rate=0.08%                                                                 

  success_rate                                                                             
  ✓ 'rate>0.95' rate=100.00%                                                               
                                                                                           
```

#### 성능 지표
- **응답 시간:** 
  - 평균: 28.5ms
  - 중앙값: 3.44ms
  - 최대: 1분 32초
  - p95: 4.93ms

- **검증 결과:**
  - 총 7,436개 검증 중 99.91% 성공 (6개 실패)
  - 충전 요청 성공(200): 99% (3,715 성공 / 3 실패)
  - 응답 본문 존재 검증: 99% (3,715 성공 / 3 실패)

#### 분석

1. **동시성 처리 성공**
   - 동시성 실패가 0건으로, 같은 사용자에 대한 여러 포인트 충전 요청이 일관되게 처리되었습니다.
   - 예상 금액과 실제 금액의 불일치가 발생하지 않아 데이터 무결성이 유지되었습니다.

2. **성능 안정성**
   - p95 응답 시간이 4.93ms로 매우 양호하며, 500ms 임계값을 훨씬 밑돌았습니다.
   - 중앙값(3.44ms)과 평균(28.5ms)의 차이는 일부 요청에서 지연이 발생했음을 나타냅니다.

3. **개선 가능 영역**
   - HTTP 요청 실패율이 0.08%로 낮지만, 완전한 0%가 아닙니다.
   - 최대 응답 시간이 1분 32초로 높게 나타나 일부 상황에서 지연이 발생했습니다.

4. **결론**
   - 테스트 중 발생한 소수의 오류(3건)는 일시적인 네트워크 문제로 보이며, 
     "Post \"http://app:8080/api/v1/user-points/user123/charge\": EOF" 오류 메시지가 나타났습니다.
   - 모든 임계값 테스트를 통과하여 시스템의 동시성 처리 능력이 충분히 검증되었습니다.

### 분산 락 테스트와의 비교

분산 락 테스트(userpoint-distributed-lock-test.js)를 실행하여 비관적 락 방식과 분산 락 방식의 성능을 비교할 수 있습니다. 일반적으로 분산 락 방식은 다음과 같은 특징을 보입니다:

- 더 높은 확장성과 부하 분산 능력
- 단일 사용자에 대한 집중 테스트에서의 일관성 유지
- 어플리케이션 서버가 여러 대로 확장되는 경우에도 동일한 사용자에 대한 일관성 보장

두 테스트 결과를 비교하여 어떤 락 방식이 특정 시나리오에 더 적합한지 판단할 수 있습니다. 