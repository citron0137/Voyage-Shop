# K6 부하 테스트 스크립트

## 개요

이 디렉토리에는 Voyage-Shop 애플리케이션의 부하 테스트를 위한 k6 스크립트가 포함되어 있습니다. 
각 스크립트는 특정 기능의 성능을 테스트하도록 설계되었습니다.

## 테스트 스크립트 설명

### 1. voyage-shop-load-test.js

전체 애플리케이션에 대한 일반적인 부하 테스트를 수행합니다.

**주요 특징:**
- 인기 상품 순위 조회 API에 집중된 테스트
- 상품 목록, 상품 상세, 포인트 잔액, 쿠폰 이벤트 등 다양한 API 호출
- 자연스러운 사용자 흐름 시뮬레이션을 위한 랜덤 대기 시간

### 2. userpoint-load-test.js

포인트 충전 및 사용에 관한 동시성 테스트를 수행합니다.

**주요 특징:**
- 동일 사용자에 대한 동시 포인트 충전/사용 요청 테스트
- 비관적 락 동작 확인을 위한 배치 요청
- 트랜잭션 히스토리 조회 기능 테스트

### 3. product-stock-load-test.js

상품 재고 증감에 관한 동시성 테스트를 수행합니다.

**주요 특징:**
- 인기 상품에 대한 재고 증감 동시 요청 테스트
- 다수의 사용자가 동시에 재고를 감소시키는 상황 시뮬레이션
- 재고 부족 상황의 적절한 오류 처리 검증

### 4. voyage-shop-load-test-lgtm.js

LGTM(Loki, Grafana, Tempo, Mimir) 스택과 통합된 테스트 스크립트입니다.

**주요 특징:**
- Prometheus로 메트릭 전송
- Loki로 로그 전송
- Tempo로 트레이싱 데이터 전송
- 상세한 사용자 행동 추적 및 성능 지표 수집

## 실행 방법

### 기본 실행

```bash
# 일반 부하 테스트 실행
k6 run voyage-shop-load-test.js

# 포인트 동시성 테스트 실행
k6 run userpoint-load-test.js

# 상품 재고 동시성 테스트 실행
k6 run product-stock-load-test.js
```

### LGTM 스택과 함께 실행

먼저, Docker Compose를 사용하여 LGTM 스택을 실행합니다:

```bash
# LGTM 스택 실행
docker compose -f docker-compose.yml up -d
```

그런 다음, LGTM 통합 스크립트를 실행합니다:

```bash
# 확장 모듈을 포함한 k6 설치 (Linux/macOS)
bash xk6-install.sh

# 확장 모듈을 포함한 k6 설치 (Windows)
.\xk6-install.ps1

# LGTM 통합 스크립트 실행
./k6 run voyage-shop-load-test-lgtm.js
```

### Grafana와 연동하여 실행

```bash
# InfluxDB에 결과 저장하고 Grafana로 시각화
k6 run --out influxdb=http://localhost:8086/k6 voyage-shop-load-test.js
```

### 사용자 수 조정하여 실행

```bash
# VU(Virtual User) 수를 조정하여 실행
k6 run --vus 50 --duration 3m voyage-shop-load-test.js
```

## LGTM 스택 구성 요소

이 프로젝트는 다음 구성 요소를 포함하는 LGTM 스택을 사용합니다:

1. **Loki**: 로그 데이터 수집 및 쿼리
   - 포트: 3100
   - 역할: k6 테스트 중 발생하는 로그 데이터 수집

2. **Grafana**: 시각화 대시보드
   - 포트: 3000
   - 역할: 수집된 모든 데이터(메트릭, 로그, 트레이스)를 통합 대시보드로 시각화

3. **Tempo**: 분산 트레이싱
   - 포트: 3200, 4317(OTLP gRPC), 4318(OTLP HTTP)
   - 역할: 요청 흐름을 추적하여 성능 병목 지점 식별

4. **Prometheus**: 메트릭 저장 및 쿼리
   - 포트: 9090
   - 역할: k6 테스트에서 생성된 메트릭 데이터 수집 및 저장

## 테스트 결과 분석

테스트 결과를 분석할 때 주목해야 할 주요 지표:

1. **응답 시간(Response Time)**
   - p95, p99 응답 시간이 설정된 임계값 내에 있는지 확인

2. **오류율(Error Rate)**
   - 전체 요청 중 실패한 요청의 비율 검토

3. **처리량(Throughput)**
   - 초당 처리된 요청 수(RPS) 확인

4. **비관적 락 동작**
   - 동시 요청 시 적절한 직렬화가 이루어지는지 검증

5. **시스템 리소스 사용률**
   - CPU, 메모리, 데이터베이스 연결 등의 리소스 사용량 모니터링
   
6. **트레이싱 데이터**
   - 요청 흐름을 분석하여 병목 지점 식별
   - 느린 API 엔드포인트 및 트랜잭션 추적 