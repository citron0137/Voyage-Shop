# 사용자 포인트 API 동시성 테스트 가이드

이 디렉토리에는 사용자 포인트 API의 동시성 처리 능력을 테스트하기 위한 k6 스크립트가 포함되어 있습니다.

## 테스트 스크립트 설명

1. **userpoint-concurrency-test.js**
   - 동일한 사용자 ID에 대한 포인트 충전 동시성 테스트
   - 비관적 락(Pessimistic Lock) 구현 기반 성능 측정용
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
# 테스트 1: 비관적 락만 적용 테스트
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

# 테스트 2: 비관적 락 + 분산 락 테스트
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

# 단일 사용자 집중 테스트만 실행 (비관적 락 + 분산 락)
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
# 테스트 1: 비관적 락만 적용된 테스트
k6 run --env API_HOST=localhost:8080 k6-tests/userpoint-concurrency-test.js

# 테스트 2: 비관적 락 + 분산 락 적용된 테스트
k6 run --env API_HOST=localhost:8080 k6-tests/userpoint-distributed-lock-test.js
```

## 테스트 메트릭 설명

### userpoint-concurrency-test.js 메트릭 (비관적 락)

- **concurrency_failures**: 동시성 처리 실패 횟수
- **expected_amount_mismatch**: 예상 금액과 실제 금액 불일치 횟수
- **success_rate**: 요청 성공률

### userpoint-distributed-lock-test.js 메트릭 (비관적 락 + 분산 락)

- **point_charge_errors**: 포인트 충전 요청 실패 횟수
- **point_charge_time**: 포인트 충전 요청 응답 시간 (ms)
- **point_charge_success**: 포인트 충전 요청 성공률
- **point_consistency_failures**: 포인트 일관성 검증 실패 횟수

## 테스트 결과 비교

세 가지 테스트 시나리오의 결과를 비교하여 다음을 확인할 수 있습니다:

1. 비관적 락 vs 비관적 락 + 분산 락 vs 분산 락만 적용의 성능 차이
2. 각 방식의 동시성 이슈 해결 능력
3. 응답 시간 변화 및 오버헤드 비교
4. 대규모 부하에서의 안정성 차이

## 테스트 조정

필요에 따라 다음 항목을 조정할 수 있습니다:

- `options.scenarios`: 부하 테스트 시나리오 설정
- `TEST_USER_ID` 또는 `singleTestUserId`: 테스트 대상 사용자 ID
- `CHARGE_AMOUNT` 또는 `chargeAmount`: 충전 금액
- `thresholds`: 성능 임계값 설정 

## 테스트 결과 예시

### 테스트 1: 비관적 락만 적용한 테스트 결과

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

### 테스트 2: 비관적 락 + 분산 락 동시 적용 테스트 결과

#### 테스트 환경
- 테스트 기간: 2분
- 최대 VU(가상 사용자): 82명
- 총 반복 실행: 3,718회
- 초당 평균 요청: 30.4회/초

#### 주요 임계값 결과
```
█ THRESHOLDS                                                                               
  concurrency_failures
  ✓ 'count<10' count=0                                                                     
  
  expected_amount_mismatch
  ✓ 'count<5' count=0                                                                      

  http_req_duration                                                                        
  ✓ 'p(95)<500' p(95)=7.42ms                                                               

  http_req_failed                                                                          
  ✓ 'rate<0.05' rate=0.00%                                                                 

  success_rate                                                                             
  ✓ 'rate>0.95' rate=100.00%                                                               
```

#### 성능 지표
- **응답 시간:** 
  - 평균: 128.84ms
  - 중앙값: 3.5ms
  - 최대: 1분 32초
  - p95: 7.42ms

- **검증 결과:**
  - 총 7,436개 검증 중 100% 성공
  - 충전 요청 모두 성공(200): 100%
  - 동시성 처리 실패: 0건
  - 예상 금액과 실제 금액 불일치: 0건

#### 분석

1. **이중 락의 영향**
   - 비관적 락과 분산 락 모두 적용되어 p95 응답 시간이 4.93ms에서 7.42ms로 증가
   - 평균 응답 시간이 28.5ms에서 128.84ms로 크게 증가
   - 두 락킹 메커니즘의 오버헤드가 중첩되어 성능 저하 발생

2. **안정성 향상**
   - HTTP 요청 실패율이 0.08%에서 0%로 개선
   - 이중 락을 통한 데이터 일관성 보장 능력 향상

3. **분산 락 적용의 비용**
   - 응답 시간 증가는 Redis 서버와의 네트워크 통신 및 AOP 처리 오버헤드로 인한 것
   - 동일한 트랜잭션에 두 가지 락 메커니즘이 적용되어 오버헤드 중첩

### 테스트 3: 분산 락만 적용 테스트 (진행 예정)

현재 진행 중이거나 계획 중인 테스트로, 비관적 락을 제거하고 분산 락만 적용한 환경에서의 성능을 측정할 예정입니다. 이 테스트에서는 다음 내용을 확인할 계획입니다:

- 분산 락만 사용 시 응답 시간 변화
- 비관적 락 제거로 인한 성능 개선 여부
- 분산 환경에서의 확장성과 일관성 유지 능력
- Redis 기반 분산 락의 순수한 오버헤드 측정

이 테스트 결과는 향후 추가될 예정입니다.

## 분산 락 적용 시 성능 고려 사항

분산 락 기반 시스템을 비관적 락 시스템과 비교했을 때, 이론적인 장점에도 불구하고 실제 환경에서는 예상보다 성능이 저하될 수 있습니다. 테스트 결과에서 최대 응답 시간이 1분 32초로 측정된 것처럼, 특정 상황에서 상당한 지연이 발생할 수 있습니다.

### 분산 락 적용 시 성능 저하 요인

1. **네트워크 오버헤드**
   - Redis와의 통신은 네트워크 호출이 필요하며, 이는 로컬 DB 락보다 더 많은 지연시간 발생
   - 분산 락 구현에서는 최소 2번의 네트워크 호출 발생 (락 획득, 락 해제)
   - 네트워크 지연이나 불안정성이 있는 환경에서 응답 시간 변동성 증가

2. **Redis 서버 부하**
   - Redis 서버의 현재 부하 상태에 따라 락 획득/해제 시간이 달라질 수 있음
   - 여러 서비스가 동일한 Redis 서버를 공유하는 경우 성능 간섭 발생 가능
   - Redis 서버의 메모리 사용량, CPU 사용률이 높을 때 지연 발생

3. **AOP 처리 오버헤드**
   - `@DistributedLock` 어노테이션 처리를 위한 AOP 프록시 생성 및 메소드 호출 가로채기 과정에서 오버헤드
   - 리플렉션을 사용한 메소드 정보 추출은 런타임 성능에 영향
   - 락 획득/해제 로직이 비즈니스 로직에 추가되어 실행 시간 증가

4. **락 획득 경쟁 및 재시도**
   - 동일 키에 대한 락 경쟁이 심할 경우 대기 시간 발생
   - 락 획득 실패 시 재시도 로직으로 인한 추가 지연
   - 특히 단일 사용자에 대한 집중 요청 시 락 획득 경쟁 심화

5. **분산 시스템 복잡성**
   - 분산 락 관리를 위한 추가 코드 및 로직으로 인한 복잡성 증가
   - 오류 처리 및 예외 상황 대응을 위한 추가 로직 필요

### 성능 개선을 위한 제안

분산 락을 사용하면서 성능을 최적화하기 위한 방안은 다음과 같습니다:

1. **Redis 서버 최적화**
   - Redis 서버를 애플리케이션 서버와 지리적으로 가까운 곳에 배치하여 네트워크 지연 최소화
   - Redis 클러스터 구성으로 부하 분산
   - 충분한 메모리와 CPU 리소스 할당

2. **락 전략 최적화**
   - 락 키 설계 최적화로 불필요한 락 경합 감소
   - 서비스별 또는 데이터 영역별로 세분화된 락 키 사용
   - 락 획득 타임아웃 적절히 설정

3. **클라이언트 연결 최적화**
   - Redis 클라이언트 커넥션 풀 튜닝
   - 적절한 연결 재사용 정책 적용
   - 장기 실행 트랜잭션의 경우 락 세분화 고려

4. **하이브리드 접근 방식**
   - 트래픽이 적은 서비스나 단일 서버 환경에서는 비관적 락 사용
   - 분산 락이 반드시 필요한 서비스에만 선택적 적용
   - 성능 임계치를 초과하는 경우 대체 전략(예: 이벤트 기반 비동기 처리) 검토

5. **모니터링 및 경보**
   - 락 획득/해제 시간, 경합률 등의 메트릭 모니터링
   - 성능 저하 시 즉각적인 알림 설정
   - 성능 데이터 기반의 지속적인 최적화

실제 운영 환경에서는 비관적 락과 분산 락의 장단점을 고려하여, 서비스 규모와 요구사항에 맞게 선택하는 것이 중요합니다. 특히 단일 서버 환경이거나 트래픽이 예측 가능한 상황에서는 비관적 락이 더 효율적일 수 있습니다. 반면, 여러 서버에 걸친 일관성이 중요한 경우에는 약간의 성능 저하를 감수하고 분산 락을 적용하는 것이 바람직합니다. 

## 테스트의 한계와 향후 개선 사항

현재 테스트에서 확인된, 개선이 필요한 영역은 다음과 같습니다:

1. **최대 부하 한계 테스트 부족**
   - 현재 테스트는 최대 82명의 가상 사용자로 제한되었으나, 실제 시스템의 한계점을 확인하기 위해 더 많은 동시 사용자(500명 이상)를 대상으로 한 테스트가 필요함
   - 시스템 성능의 임계점 파악을 위한 단계적 부하 증가 테스트 필요

2. **장시간 테스트 부재**
   - 2분 동안의 짧은 테스트로는 메모리 누수, 리소스 고갈 등 장시간 운영 시 발생할 수 있는 문제점 파악이 어려움
   - 최소 1시간 이상의 지속적인 부하 테스트를 통한 시스템 안정성 검증 필요

3. **장애 상황 시뮬레이션 부족**
   - Redis 서버 장애, 네트워크 단절 등 인프라 장애 상황에서의 시스템 동작 테스트 필요
   - 분산 락 획득 실패 시 적절한 폴백(fallback) 메커니즘 검증 필요
   - 락 획득 타임아웃 상황에서의 시스템 응답 및 복구 능력 확인 필요

4. **복합 트랜잭션 시나리오 부재**
   - 현재 테스트는 단일 포인트 충전 작업에 국한되어 있음
   - 포인트 충전과 동시에 포인트 사용, 쿠폰 적용 등 복합적인 트랜잭션이 발생하는 실제 사용 시나리오 테스트 필요

5. **네트워크 지연 시뮬레이션 부재**
   - 실제 분산 환경에서 발생 가능한 네트워크 지연이나 패킷 손실 상황을 시뮬레이션하지 않음
   - 다양한 네트워크 조건에서의 분산 락 동작 검증 필요

6. **락 관련 메트릭 모니터링 부족**
   - 락 획득 시간, 대기 시간, 락 경합률 등 분산 락 관련 상세 메트릭 측정 부재
   - 이러한 메트릭을 통한 시스템 튜닝 및 최적화 가능성 확인 필요

7. **다양한 클라이언트 환경 테스트 부족**
   - 다양한 클라이언트 환경(모바일, 웹, API 등)에서의 테스트 부재
   - 실제 사용자 패턴을 반영한 더 현실적인 테스트 시나리오 구성 필요

이러한 한계점들을 개선하여 추가 테스트를 진행한다면, 분산 락 구현의 실제 운영 환경 적합성을 더욱 명확하게 검증할 수 있을 것입니다. 특히 장애 상황에서의 동작과 장시간 운영 시 안정성은 실제 서비스 배포 전 반드시 확인해야 할 중요한 요소입니다.