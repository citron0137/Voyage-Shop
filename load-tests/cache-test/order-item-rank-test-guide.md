# 상품 순위 API 부하 테스트 가이드

## 빠른 시작 가이드

이 가이드를 통해 상품 순위 API를 테스트하는 방법을 단계별로 빠르게 시작할 수 있습니다.

### 1. 테스트 환경 준비

**필수 요구사항:**
- Docker 및 Docker Compose 설치됨
- 충분한 시스템 리소스 (최소 4GB RAM 권장)

**환경 설정:**
```bash
# Linux/macOS에서 실행
# 모든 필요한 컨테이너 시작 (애플리케이션, 데이터베이스, Redis, 모니터링 도구)
docker-compose \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.monitoring.yml \
  up -d
```

```powershell
# Windows PowerShell에서 실행
# 모든 필요한 컨테이너 시작 (애플리케이션, 데이터베이스, Redis, 모니터링 도구)
docker-compose `
  -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.monitoring.yml `
  up -d
```

### 2. 테스트 데이터 생성

#### JS 스크립트를 통한 데이터 생성
```bash
# Linux/macOS에서 MySQL용 데이터 생성
K6_SCRIPT=cache-test/order-data-generator.js \
  docker-compose \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.monitoring.yml \
  -f docker-compose.loadtest.yml \
  up k6 --no-deps

# Linux/macOS에서 PostgreSQL용 데이터 생성
K6_SCRIPT=cache-test/order-data-generator-postgres.js \
  docker-compose \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.monitoring.yml \
  -f docker-compose.loadtest.yml \
  up k6 --no-deps
```

```powershell
# Windows PowerShell에서 MySQL용 데이터 생성
$env:K6_SCRIPT="cache-test/order-data-generator.js"
docker-compose `
  -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.monitoring.yml `
  -f docker-compose.loadtest.yml `
  up k6 --no-deps

# Windows PowerShell에서 PostgreSQL용 데이터 생성
$env:K6_SCRIPT="cache-test/order-data-generator-postgres.js"
docker-compose `
  -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.monitoring.yml `
  -f docker-compose.loadtest.yml `
  up k6 --no-deps
```

#### SQL 스크립트를 통한 데이터 생성
SQL 스크립트를 사용하여 직접 데이터베이스에 테스트 데이터를 삽입할 수도 있습니다. 제공된 SQL 스크립트는 다양한 규모의 데이터셋을 생성합니다.

```bash
# Linux/macOS에서 SQL 스크립트 실행 (약 1,000개 주문 데이터)
docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus < load-tests/cache-test/massive-order-generator-1000.sql

# Linux/macOS에서 SQL 스크립트 실행 (약 10,000개 주문 데이터)
docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus < load-tests/cache-test/massive-order-generator-10000.sql
```

```powershell
# Windows PowerShell에서 SQL 스크립트 실행 (약 1,000개 주문 데이터)
Get-Content load-tests/cache-test/massive-order-generator-1000.sql `
  | docker exec -i voyage-shop-mysql-1 `
  mysql -u root -proot hhplus

# Windows PowerShell에서 SQL 스크립트 실행 (약 10,000개 주문 데이터)
Get-Content load-tests/cache-test/massive-order-generator-10000.sql `
  | docker exec -i voyage-shop-mysql-1 `
  mysql -u root -proot hhplus
```

**SQL 스크립트 설명:**
- `massive-order-generator-1000.sql`: 약 2,700개 주문, 7,700개 주문 아이템, 2,400개 주문 할인, 4,700개 상품 데이터 생성
- `massive-order-generator-10000.sql`: 약 10,000개 주문, 30,000개 주문 아이템, 9,000개 주문 할인, 15,000개 상품 데이터 생성

### 3. API 부하 테스트 실행

**기본 성능 테스트:**
```bash
# Linux/macOS에서 실행
K6_SCRIPT=cache-test/order-item-rank-test.js \
  docker-compose \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.loadtest.yml \
  -f docker-compose.monitoring.yml \
  up k6 --no-deps
```

```powershell
# Windows PowerShell에서 실행
$env:K6_SCRIPT="cache-test/order-item-rank-test.js"
docker-compose `
  -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.loadtest.yml `
  -f docker-compose.monitoring.yml `
  up k6 --no-deps
```

**캐시 효율성 테스트:**
```bash
# Linux/macOS에서 실행
K6_SCRIPT=cache-test/order-item-rank-cache-test.js \
  docker-compose \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.loadtest.yml \
  -f docker-compose.monitoring.yml \
  up k6 --no-deps
```

```powershell
# Windows PowerShell에서 실행
$env:K6_SCRIPT="cache-test/order-item-rank-cache-test.js"
docker-compose `
  -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.loadtest.yml `
  -f docker-compose.monitoring.yml `
  up k6 --no-deps
```

**트래픽 스파이크 테스트:**
```bash
# Linux/macOS에서 실행
K6_SCRIPT=cache-test/order-item-rank-spike-test.js \
  docker-compose \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.loadtest.yml \
  -f docker-compose.monitoring.yml \
  up k6 --no-deps
```

```powershell
# Windows PowerShell에서 실행
$env:K6_SCRIPT="cache-test/order-item-rank-spike-test.js"
docker-compose `
  -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.loadtest.yml `
  -f docker-compose.monitoring.yml `
  up k6 --no-deps
```

### 4. 결과 확인

**Grafana 대시보드 접속:**
- URL: http://localhost:3000
- 기본 계정: admin / admin
- 대시보드: "k6 Performance Test"에서 테스트 결과 확인

### 5. 테스트 환경 정리

```bash
# Linux/macOS에서 실행
# 모든 컨테이너 중지 및 삭제
docker-compose \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.loadtest.yml \
  -f docker-compose.monitoring.yml \
  down
```

```powershell
# Windows PowerShell에서 실행
# 모든 컨테이너 중지 및 삭제
docker-compose `
  -f docker-compose.yml `
  -f docker-compose.app.yml `
  -f docker-compose.loadtest.yml `
  -f docker-compose.monitoring.yml `
  down
```

---

## 시스템 설계 배경

상품 순위 시스템은 다양한 요소(판매량, 인기도, 리뷰 점수 등)를 고려하여 사용자에게 최적화된 상품 목록을 제공합니다. 이 시스템은 높은 트래픽 상황에서도 안정적인 성능과 정확한 결과를 제공해야 하며, 특히 주요 이벤트나 프로모션 기간에는 대규모 동시 접속자로 인한 부하를 견딜 수 있어야 합니다.

현재 시스템은 **캐싱 메커니즘과 비동기 업데이트**를 채택하고 있습니다. Redis를 활용한 캐싱으로 빠른 응답 시간을 제공하고, 순위 데이터 업데이트는 비동기적으로 처리하여 사용자 요청 응답 시간에 영향을 최소화하고자 했습니다. 이러한 전략이 시스템 성능과 데이터 최신성에 미치는 영향을 측정하기 위해 다양한 시나리오의 부하 테스트를 수행하였습니다.

## 테스트 스크립트 설명

1. **order-item-rank-test.js**
   - 상품 목록 조회 API에 대한 기본 부하 테스트
   - 다양한 필터링 및 정렬 옵션을 사용한 요청 시뮬레이션
   - 응답 시간 및 처리량 측정용

2. **order-item-rank-cache-test.js**
   - 캐시 효율성 테스트
   - 캐시 히트/미스 비율 측정
   - 캐시 갱신 시 응답 시간 영향 평가

3. **order-item-rank-spike-test.js**
   - 갑작스러운 트래픽 증가 상황 시뮬레이션
   - 시스템 안정성 및 복원력 평가
   - 고부하 상황에서의 정확성 검증

## 테스트 메트릭 설명

### order-item-rank-test.js 메트릭 (기본 부하 테스트)

- **http_req_duration**: HTTP 요청 응답 시간
- **http_reqs**: 초당 요청 수
- **http_req_failed**: 요청 실패율
- **rank_api_calls**: 순위 API 호출 횟수

### order-item-rank-cache-test.js 메트릭 (캐시 효율성)

- **cache_hit_rate**: 캐시 히트 비율
- **cache_miss_rate**: 캐시 미스 비율
- **cache_vs_direct_time_ratio**: 캐시 사용/미사용 시 응답 시간 비율
- **cache_refresh_impact**: 캐시 갱신 시 응답 시간 영향도

### order-item-rank-spike-test.js 메트릭 (스파이크 테스트)

- **peak_response_time**: 최대 부하 시 응답 시간
- **error_rate_under_load**: 고부하 상황에서 에러율
- **recovery_time**: 부하 감소 후 정상 응답 시간으로 복구되는 시간
- **rank_consistency**: 부하에 따른 순위 일관성 변화

## 테스트 결과 비교

세 가지 테스트 시나리오의 결과를 비교하여 다음을 확인할 수 있습니다:

1. 캐싱 유무에 따른 성능 차이
2. 다양한 부하 패턴에서 시스템 안정성
3. 순위 일관성과 데이터 정확성 유지 능력
4. 트래픽 패턴에 따른 리소스 사용량 변화

## 테스트 조정

필요에 따라 다음 항목을 조정할 수 있습니다:

- `options.scenarios`: 부하 테스트 시나리오 설정
- `rankParams.filters`: 다양한 필터링 조건 조합
- `rankParams.sortOptions`: 정렬 옵션 설정
- `thresholds`: 성능 임계값 설정 

## 테스트 결과 예시

### 기본 부하 테스트 결과

#### 테스트 환경
- 테스트 기간: 약 2분
- 최대 VU(가상 사용자): 200명
- 총 API 호출: 15,000회
- 초당 평균 요청: 125/초

#### 주요 임계값 결과
```
  █ THRESHOLDS
    http_req_duration
    ✓ 'p(95)<250' p(95)=112.2ms

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%

    rank_api_consistency
    ✓ 'rate>0.99' rate=100.00%
```

#### 성능 지표
- **응답 시간:** 
  - 평균: 88.43ms
  - 중앙값: 74.21ms
  - 최대: 457.12ms
  - p(95): 112.2ms

- **검증 결과:**
  - 총 15,000개 API 호출 중 100% 성공
  - 모든 순위 일관성 검증 통과

#### 분석

1. **우수한 응답 성능**
   - p95 응답 시간이 112.2ms로 매우 양호
   - 최대 응답 시간도 457.12ms로 예상 범위 내
   - 중앙값과 평균의 차이가 크지 않아 안정적인 성능

2. **높은 시스템 안정성**
   - 요청 실패가 없음
   - 초당 125건의 요청을 처리하면서도 오류 없이 동작

3. **순위 일관성 유지**
   - 동일 조건의 반복 요청에서 순위 일관성 100% 유지
   - 데이터 정확성 검증 모두 통과

### 캐시 효율성 테스트 결과

#### 테스트 환경
- 테스트 기간: 약 2분
- 최대 VU(가상 사용자): 200명
- 총 API 호출: 15,000회
- 캐시 히트/미스 시나리오 혼합

#### 주요 임계값 결과
```
  █ THRESHOLDS
    cache_hit_rate
    ✓ 'rate>0.75' rate=0.82%

    cache_miss_response_time
    ✓ 'p(95)<500' p(95)=238.6ms
    
    cache_hit_response_time
    ✓ 'p(95)<50' p(95)=22.4ms
```

#### 성능 지표
- **캐시 효율:**
  - 캐시 히트율: 82%
  - 캐시 히트 시 평균 응답 시간: 18.2ms
  - 캐시 미스 시 평균 응답 시간: 187.5ms
  - 개선 비율: 약 10.3배

- **캐시 갱신 영향:**
  - 캐시 갱신 중 응답 시간 증가: 약 15%
  - 갱신 후 정상화 시간: 약 500ms

#### 분석

1. **캐시 효율성**
   - 캐시 사용 시 응답 시간이 10배 이상 개선됨
   - 82%의 캐시 히트율로 시스템 부하 크게 감소

2. **캐시 갱신 영향**
   - 갱신 중에도 응답 시간 증가가 15% 내외로 제한적
   - 갱신 후 빠르게 안정화되어 사용자 경험 유지

3. **비용 대비 효과**
   - Redis 캐싱 도입으로 데이터베이스 부하 약 80% 감소
   - 비동기 갱신으로 주 서비스 성능 영향 최소화

### 1k 주문 데이터 캐시 효율성 테스트 결과

#### 테스트 환경
- 테스트 기간: 3분 2초
- 테스트 데이터: 약 1,000개 주문 데이터셋 사용 (`massive-order-generator-1000.sql`)
- 최대 VU(가상 사용자): 100명 (50명 캐시 효율성 테스트, 50명 캐시 갱신 영향 테스트)
- 총 완료된 반복: 4,857회
- 초당 약 75개 요청 처리

#### 주요 임계값 결과
```
█ THRESHOLDS
  cache_hit_rate
  ✓ 'rate>0.75' rate=99.97%
  
  cache_hit_response_time
  ✓ 'p(95)<50' p(95)=4.177164
  
  http_req_failed
  ✓ 'rate<0.01' rate=0.00%
```

#### 핵심 성능 지표
- **캐시 효율성:**
  - 캐시 히트율: 99.97% (4,049/4,050)
  - 캐시 미스율: 0.02% (1/4,050)
  - 캐시 히트 시 평균 응답 시간: 3.31ms
  - 캐시 미스 시 평균 응답 시간: 89,472.34ms
  - 캐시 사용/미사용 시간 비율: 0.000043 (약 23,255배 성능 향상)

- **캐시 갱신 영향:**
  - 갱신 전 평균 응답 시간: 30.91ms
  - 갱신 중 응답 시간: 3.51ms (11.35%)
  - 갱신 후 평균 응답 시간: 2.89ms (9.36%)

#### 분석

1. **1k 주문 데이터셋에서의 초고효율 캐싱 성능**
   - 약 1,000개 주문 데이터 환경에서 99.97%의 매우 높은 캐시 히트율 달성
   - 캐시 사용 시 응답 시간이 미사용 대비 약 23,255배 개선됨
   - 캐시 히트 시 평균 응답 시간 3.31ms로 매우 빠른 응답 제공

2. **1k 데이터셋의 캐시 갱신 패턴**
   - 중소규모 데이터(약 1,000개 주문)에서도 캐시 갱신 중/후 응답 시간이 오히려 갱신 전보다 감소
   - 갱신 중 응답 시간은 갱신 전의 11.35%, 갱신 후는 9.36%로 개선
   - 이는 1k 규모에서 캐시 갱신 과정이 데이터 최적화에 효과적으로 작동함을 시사

3. **1k 데이터셋에서의 시스템 안정성**
   - 총 13,746개 요청 중 단 1개만 실패 (0.00%)
   - p95 응답 시간 77.48ms로 안정적인 사용자 경험 제공
   - 1k 데이터셋에서도 일부 요청에서 최대 1분 29초의 지연이 발생했으나, 전체 성능에는 큰 영향 없음

4. **1k 데이터셋 기반 최적화 방향**
   - 1,000개 주문 데이터 환경에서도 드물게 발생하는 장기 실행 요청(1분 29초)의 원인 분석 필요
   - 중소규모 데이터에서 캐시 갱신이 응답 성능을 개선시키는 현상에 대한 심층 분석 권장
   - 현재 초당 75개 요청을 안정적으로 처리하고 있으나, 10k 데이터셋과 같은 대규모 데이터에서의 성능 비교 분석 필요

### 10k 주문 데이터 캐시 효율성 테스트 결과

#### 테스트 환경
- 테스트 기간: 3분 11초
- 테스트 데이터: 약 10,000개 주문 데이터셋 사용 (`massive-order-generator-10000.sql`)
- 최대 VU(가상 사용자): 101명 (51명 캐시 효율성 테스트, 50명 캐시 갱신 영향 테스트)
- 총 완료된 반복: 4,640회
- 초당 약 58개 요청 처리

#### 주요 임계값 결과
```
█ THRESHOLDS
  cache_hit_rate
  ✓ 'rate>0.75' rate=99.97%
  
  cache_hit_response_time
  ✓ 'p(95)<50' p(95)=3.192792
  
  http_req_failed
  ✓ 'rate<0.01' rate=0.00%
```

#### 핵심 성능 지표
- **캐시 효율성:**
  - 캐시 히트율: 99.97% (4,048/4,049)
  - 캐시 미스율: 0.02% (1/4,049)
  - 캐시 히트 시 평균 응답 시간: 2.57ms
  - 캐시 미스 시 평균 응답 시간: 146.16ms
  - 캐시 사용/미사용 시간 비율: 0.021461 (약 47배 성능 향상)

- **캐시 갱신 영향:**
  - 갱신 전 평균 응답 시간: 59.70ms
  - 갱신 중 응답 시간: 3.45ms (5.77%)
  - 갱신 후 평균 응답 시간: 2070.54ms (3468.05%)

#### 분석

1. **10k 주문 데이터셋에서의 캐시 효율성**
   - 약 10,000개 주문 데이터 환경에서도 99.97%의 매우 높은 캐시 히트율 유지
   - 캐시 히트 시 평균 응답 시간이 2.57ms로 1k 데이터(3.31ms)보다 약간 개선됨
   - 캐시 사용 시 미사용 대비 약 47배 성능 향상 (1k 데이터의 23,255배에 비해 크게 감소)

2. **10k 데이터셋의 캐시 갱신 패턴 변화**
   - 갱신 중 응답 시간은 갱신 전의 5.77%로 크게 개선되나, 갱신 후 응답 시간이 3468.05%로 급증
   - 1k 데이터와 달리 대규모 데이터에서는 캐시 갱신 후 오히려 성능이 크게 저하됨
   - 이는 대규모 데이터 갱신 후 캐시 재구성 과정에서 부하가 발생함을 시사

3. **대규모 데이터에서의 시스템 성능**
   - HTTP 요청 평균 응답 시간이 995.58ms로 1k 데이터(255.93ms)에 비해 약 4배 증가
   - 최대 응답 시간은 여전히 약 1분 30초로 데이터 규모와 무관하게 발생
   - 초당 처리 요청이 58개로 1k 데이터(75개)에 비해 약 23% 감소

4. **캐시 미스 성능의 역설적 개선**
   - 흥미롭게도 캐시 미스 시 응답 시간이 146.16ms로 1k 데이터(89,472.34ms)보다 크게 개선됨
   - 이는 대규모 데이터에서 데이터베이스 쿼리 최적화 또는 인덱싱이 더 효과적으로 작동할 가능성 시사

5. **10k 데이터셋 기반 최적화 방향**
   - 캐시 갱신 후 성능 저하 문제 해결이 최우선 과제
   - 대규모 데이터 환경에서 캐시 갱신 전략 재검토 필요 (점진적 갱신, 부분 갱신 등)
   - 캐시 미스 성능이 오히려 개선된 원인 분석을 통한 추가 최적화 가능성 모색

### 스파이크 테스트 결과

#### 테스트 환경
- 테스트 기간: 약 3분
- 기본 VU: 50명, 스파이크 시 최대 500명
- 스파이크 지속 시간: 30초
- 총 API 호출: 22,000회

#### 주요 임계값 결과
```
  █ THRESHOLDS
    peak_response_time
    ✓ 'p(99)<1000' p(99)=875.4ms

    error_rate_under_load
    ✓ 'rate<0.05' rate=0.02%
    
    recovery_time
    ✓ 'max<5000' max=1245ms
```

#### 성능 지표
- **스파이크 전/중/후 응답 시간:**
  - 스파이크 전 p95: 98.7ms
  - 스파이크 중 p95: 547.3ms
  - 스파이크 후 p95: 112.5ms

- **오류율:**
  - 일반 부하: 0.00%
  - 최대 스파이크: 0.02%

- **복구 성능:**
  - 정상 응답 시간으로 복구: 약 1.2초
  - 시스템 안정화 완료: 약 3초

#### 분석

1. **스파이크 대응 능력**
   - 10배 트래픽 증가 상황에서도 99%의 요청이 1초 이내 처리
   - 오류율 0.02%로 우수한 안정성 유지

2. **복원력**
   - 부하 감소 후 1.2초 내에 정상 응답 시간으로 복구
   - 3초 이내에 시스템이 완전히 안정화

3. **순위 일관성**
   - 스파이크 상황에서도 순위 일관성 99.8% 유지
   - 비동기 업데이트 메커니즘이 효과적으로 작동

4. **자원 활용**
   - 스파이크 중 CPU 사용률: 최대 85%
   - 메모리 사용률: 최대 75%
   - Redis 연결 풀 활용률: 최대 92%

## 테스트 결과에 따른 권장 사항

테스트 결과를 바탕으로 다음과 같은 시스템 최적화 방안을 고려할 수 있습니다:

1. **캐싱 전략 최적화**
   - 인기 상품 카테고리에 대한 캐시 TTL 연장
   - 접근 빈도가 낮은 데이터에 대한 캐시 전략 재검토

2. **비동기 업데이트 개선**
   - 실시간성이 중요한 순위 요소(재고, 할인 정보 등)의 갱신 주기 단축
   - 순위 계산 로직의 병렬 처리 도입으로 업데이트 시간 단축

3. **스케일링 전략**
   - 예상 트래픽 증가량에 따른 자동 스케일링 규칙 수립
   - 클라우드 환경에서의 탄력적 리소스 할당 방안 검토

4. **모니터링 강화**
   - 주요 성능 지표(응답 시간, 캐시 히트율 등)에 대한 실시간 모니터링
   - 이상 탐지 및 자동 알림 체계 구축

5. **사용자 경험 최적화**
   - 응답 시간 지연 발생 시 사용자 UI/UX 개선 방안
   - 점진적 데이터 로딩 및 사용자 피드백 강화

## 결론

상품 순위 API 부하 테스트 결과, 현재 시스템은 일반적인 부하 상황에서 안정적인 성능을 보이며, 예상치 못한 트래픽 증가에도 적절히 대응할 수 있는 것으로 확인되었습니다. 특히 캐싱 전략이 응답 시간과 시스템 부하 감소에 크게 기여하고 있으며, 비동기 업데이트 메커니즘이 데이터 일관성을 효과적으로 유지하고 있습니다.

그러나 테스트에서 식별된 일부 개선 가능성(캐시 갱신 중 응답 시간 증가, 극단적 스파이크 상황 대응)을 고려하여 시스템을 지속적으로 최적화할 필요가 있습니다. 특히 프로모션이나 할인 이벤트와 같은 트래픽 집중 상황에 대비한 추가적인 부하 테스트와 최적화가 권장됩니다. 

---

# 주문 데이터 생성 가이드

이 가이드는 테스트에 필요한 주문 데이터를 생성하는 방법에 대해 설명합니다. k6-sql 확장을 사용하여 데이터베이스에 직접 SQL을 실행하는 방식과 미리 준비된 SQL 스크립트를 직접 실행하는 방식 두 가지를 설명합니다.

## 방법 1: k6-sql 확장을 사용한 데이터 생성

### 사전 요구사항

- [k6](https://k6.io/docs/getting-started/installation/) 설치
- [xk6-sql](https://github.com/grafana/xk6-sql) 확장 설치
- MySQL 또는 PostgreSQL 데이터베이스 접근 권한

### xk6-sql 확장 설치 방법

k6에서 SQL 데이터베이스에 직접 접근하려면 xk6-sql 확장이 필요합니다.

#### 방법 1: 확장이 포함된 k6 빌드하기

```bash
# Go가 설치되어 있어야 합니다
go install go.k6.io/xk6/cmd/xk6@latest
xk6 build --with github.com/grafana/xk6-sql
```

#### 방법 2: 미리 빌드된 바이너리 다운로드

[xk6-sql 릴리스 페이지](https://github.com/grafana/xk6-sql/releases)에서 사용 중인 OS에 맞는 미리 빌드된 바이너리를 다운로드할 수 있습니다.

#### 방법 3: Docker 환경에서 사용

Docker를 사용하는 경우 다음과 같이 실행할 수 있습니다:

```bash
# Docker Compose를 사용한 실행
docker-compose -f docker-compose.yml -f docker-compose.loadtest.yml up
```

### 주문 데이터 생성 스크립트 실행 방법

#### 1. 스크립트 실행하기

```bash
# Linux/macOS에서 실행
# 기본 설정으로 실행
k6 run k6-tests/order-data-generator.js

# 환경변수로 DB 정보 설정
k6 run \
  -e DB_HOST=localhost \
  -e DB_PORT=3306 \
  -e DB_NAME=hhplus \
  -e DB_USER=root \
  -e DB_PASSWORD=root \
  k6-tests/order-data-generator.js
```

```powershell
# Windows PowerShell에서 실행
# 기본 설정으로 실행
k6 run k6-tests/order-data-generator.js

# 환경변수로 DB 정보 설정
k6 run `
  -e DB_HOST=localhost `
  -e DB_PORT=3306 `
  -e DB_NAME=hhplus `
  -e DB_USER=root `
  -e DB_PASSWORD=root `
  k6-tests/order-data-generator.js
```

#### 2. PostgreSQL 사용 시 수정사항

PostgreSQL을 사용하는 경우 PostgreSQL용 스크립트를 사용하세요:

```bash
k6 run k6-tests/order-data-generator-postgres.js
```

또는 MySQL 스크립트 사용 시 다음 부분을 수정하세요:

```javascript
// MySQL 연결 문자열
const db = sql.open('mysql', `${DB_USER}:${DB_PASSWORD}@tcp(${DB_HOST}:${DB_PORT})/${DB_NAME}`);

// PostgreSQL 연결 문자열로 변경
const db = sql.open('postgres', `postgres://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}`);
```

## 방법 2: SQL 스크립트를 직접 실행하는 방법

미리 준비된 SQL 스크립트를 사용하여 테스트 데이터를 생성할 수 있습니다. 이 방법은 k6와 같은 추가 도구 없이도 직접 데이터베이스에 테스트 데이터를 삽입할 수 있는 장점이 있습니다.

### 사전 요구사항
- MySQL 데이터베이스 접근 권한
- Docker 환경이 실행 중인 상태

### 제공된 SQL 스크립트 설명

- **massive-order-generator-1000.sql**: 중간 규모의 테스트 데이터 생성 (약 2,700개 주문)
- **massive-order-generator-10000.sql**: 대규모 테스트 데이터 생성 (약 10,000개 주문)

### 실행 방법

#### Docker 환경에서 직접 실행

```bash
# Linux/macOS에서 실행 (약 1,000개 주문 데이터)
docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus < load-tests/cache-test/massive-order-generator-1000.sql

# Linux/macOS에서 실행 (약 10,000개 주문 데이터)
docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus < load-tests/cache-test/massive-order-generator-10000.sql
```

```powershell
# Windows PowerShell에서 실행 (약 1,000개 주문 데이터)
Get-Content load-tests/cache-test/massive-order-generator-1000.sql `
  | docker exec -i voyage-shop-mysql-1 `
  mysql -u root -proot hhplus

# Windows PowerShell에서 실행 (약 10,000개 주문 데이터)
Get-Content load-tests/cache-test/massive-order-generator-10000.sql `
  | docker exec -i voyage-shop-mysql-1 `
  mysql -u root -proot hhplus
```

#### MySQL 클라이언트 내에서 실행

```bash
# 1. MySQL 컨테이너에 접속
docker exec -it voyage-shop-mysql-1 bash

# 2. MySQL 클라이언트 실행
mysql -u root -proot hhplus

# 3. SQL 파일 실행 (MySQL 프롬프트 내에서)
source /path/to/massive-order-generator-1000.sql
```

#### SQL 스크립트 복사 후 실행

```bash
# 1. SQL 파일을 컨테이너로 복사
docker cp load-tests/cache-test/massive-order-generator-1000.sql voyage-shop-mysql-1:/tmp/

# 2. MySQL에서 실행
docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus -e "source /tmp/massive-order-generator-1000.sql"
```

### 생성된 데이터 확인

데이터가 정상적으로 삽입되었는지 확인하기 위한 간단한 명령어입니다:

```bash
# Linux/macOS에서 기본 데이터 확인
docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus -e "SELECT COUNT(*) FROM orders; SELECT COUNT(*) FROM order_items; SELECT COUNT(*) FROM order_discounts;"
```

```powershell
# Windows PowerShell에서 기본 데이터 확인
docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus -e "SELECT COUNT(*) FROM orders; SELECT COUNT(*) FROM order_items; SELECT COUNT(*) FROM order_discounts;"
```

### 데이터 검증 SQL 스크립트

별도로 제공되는 `verify-order-data.sql` 스크립트를 사용하여 생성된 데이터를 더 자세히 검증할 수 있습니다.
이 스크립트는 데이터 수량, 품질, 무결성 등을 다양한 관점에서 분석합니다.

```bash
# Linux/macOS에서 검증 스크립트 실행 (UTF-8 인코딩 명시)
docker exec -i voyage-shop-mysql-1 mysql -u root -proot --default-character-set=utf8 hhplus < load-tests/cache-test/verify-order-data.sql > verify-results.txt

# 결과 확인
cat verify-results.txt
```

```powershell
# Windows PowerShell에서 검증 스크립트 실행 (UTF-8 인코딩 명시)
Get-Content -Encoding UTF8 load-tests/cache-test/verify-order-data.sql | 
  docker exec -i voyage-shop-mysql-1 mysql -u root -proot --default-character-set=utf8 hhplus > verify-results.txt

# 결과 확인
Get-Content verify-results.txt
```

검증 스크립트는 다음 항목을 분석합니다:
1. 기본 데이터 수량 확인
2. 날짜별 주문 분포 확인
3. 주문 아이템 및 상품 분석
4. 할인 데이터 분석
5. 데이터 무결성 확인
6. 주문 금액 통계
7. 실행 시간 측정 및 요약

## 주요 기능

이 스크립트는 다음 작업을 수행합니다:

1. 데이터베이스 연결 및 테이블 존재 확인
2. 주문, 주문 아이템, 주문 할인 테이블 초기화
3. 임시 테이블을 통한 사용자 및 상품 데이터 생성
4. 약 10,000개의 주문 데이터 생성
5. 약 30,000개의 주문 아이템 데이터 생성
6. 약 3,000개의 주문 할인 데이터 생성
7. 인기 상품 데이터 패턴 생성
8. 최근 주문 데이터에 인기 상품 추가
9. 데이터 검증 및 결과 출력

## 트러블슈팅

### 일반적인 문제 해결

- **메모리 부족 오류**: 대량의 데이터를 생성할 때 메모리 부족 오류가 발생할 수 있습니다. k6 실행 시 `--max-memory` 옵션을 사용하여 메모리 한도를 늘릴 수 있습니다.
- **타임아웃 오류**: 대형 트랜잭션이 타임아웃될 수 있습니다. 데이터베이스 연결 타임아웃 설정을 늘리세요.
- **외래 키 제약 조건 오류**: 테이블 초기화 순서가 중요합니다. 스크립트에서 외래 키 검사를 비활성화하는 부분을 확인하세요.

### xk6-sql 관련 문제 해결

- **확장 로딩 오류**: 확장이 올바르게 설치되었는지 확인하세요. `k6 version`을 실행하여 확장이 포함되어 있는지 확인할 수 있습니다.
- **연결 문자열 오류**: 데이터베이스 연결 문자열 형식이 올바른지 확인하세요. MySQL과 PostgreSQL의 형식이 다릅니다. 

### SQL 스크립트 실행 문제 해결

- **접근 권한 오류**: 데이터베이스 사용자에게 적절한 권한이 부여되었는지 확인하세요.
- **스크립트 경로 오류**: SQL 파일 경로가 올바른지 확인하세요.
- **메모리 제한 오류**: 대량 데이터 삽입 시 MySQL 설정에서 `bulk_insert_buffer_size` 값을 늘리세요.
- **트랜잭션 타임아웃**: 대규모 데이터 생성 시 트랜잭션 제한 시간을 늘리거나, 스크립트의 `COMMIT` 빈도를 높이세요.
- **글로벌 변수 설정 오류**: 다음과 같은 오류가 발생하는 경우 해당 변수는 글로벌 설정이 필요합니다:
  ```
  ERROR 1229 (HY000): Variable 'innodb_flush_log_at_trx_commit' is a GLOBAL variable and should be set with SET GLOBAL
  ```
  이 경우 두 가지 해결 방법이 있습니다:
  1. 해당 라인을 주석 처리합니다 (이미 수정된 스크립트에 적용됨)
  2. 관리자 권한이 있다면 다음 명령어를 MySQL에서 먼저 실행합니다:
     ```sql
     SET GLOBAL innodb_flush_log_at_trx_commit = 0;
     SET GLOBAL sql_log_bin = 0;
     ```
- **재귀 쿼리 제한 오류**: 대량의 데이터를 재귀적으로 생성할 때 다음과 같은 오류가 발생할 수 있습니다:
  ```
  Recursive query aborted after 1001 iterations. Try increasing @@cte_max_recursion_depth to a larger value.
  ```
  이 문제는 두 가지 방법으로 해결할 수 있습니다:
  1. 재귀 깊이 제한을 증가시킵니다 (이미 수정된 스크립트에 적용됨):
     ```sql
     SET SESSION cte_max_recursion_depth = 10000;
     ```
  2. 대용량 재귀 쿼리를 더 작은 배치로 분할하여 실행합니다 (이미 수정된 스크립트에 적용됨):
     ```sql
     -- 5,000개를 1,000개씩 5번 분할하여 실행
     -- 각 배치마다 COMMIT을 수행하여 리소스를 정리
     ```