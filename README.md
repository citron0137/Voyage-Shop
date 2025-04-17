# Voyage Shop

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)


## 📑 목차

- [소개](#소개)
- [비즈니스 요구사항 정리](#비즈니스-요구사항-정리)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [DB 구조](#DB-구조)
- [테스트](#테스트)
- [모니터링 설정 (Grafana & OpenTelemetry)](#모니터링-설정-grafana-&-opentelemetry)


## 📌 소개

**Voyage Shop**은 온라인 쇼핑몰 백엔드 서비스입니다. 제품 관리, 주문 처리, 사용자 관리, 쿠폰 및 포인트 시스템을 포함하고 있습니다.

## 📚 비즈니스 요구사항 정리

프로젝트의 요구사항과 설계에 관련된 문서입니다:

**📄 요구사항 및 설계**
- [요구사항 분석](./docs/system-design/01-requirement.md)
- [시퀀스 다이어그램](./docs/system-design/02-sequance-diagram.md)
  

## 🛠 기술 스택

**언어:** Kotlin  
**프레임워크:** Spring Boot  
**빌드 도구:** Gradle  
**컨테이너화:** Docker  
**데이터베이스:** MySQL

## 🚀 시작하기

### 사전 준비

프로젝트를 실행하기 위해 다음 도구가 필요합니다:

- JDK 17 이상
- Docker 및 Docker Compose

### 환경 설정

**1. 저장소 클론**

```bash
git clone https://github.com/your-username/voyage-shop.git
cd voyage-shop
```

**2. 도커 컨테이너 실행**

```bash
docker-compose up -d
```

**3. 애플리케이션 실행**

```bash
./gradlew bootRun
```

## 📂 프로젝트 구조

프로젝트는 레이어드 아키텍처 패턴을 따릅니다. 자세한 내용은 아래 문서를 참조하세요:

- [프로젝트 컨벤션](./docs/conventions/common-conventions.md)
- [레이어드 아키텍처](./docs/conventions/layered-architecture.md)
- [패키지 구조](./docs/conventions/package-structure.md)

## 📊 DB 구조

<img src="https://cdn-icons-png.flaticon.com/512/2906/2906274.png" width="80" alt="Database">

프로젝트의 데이터베이스 스키마 설계에 관한 문서입니다:

**[ERD 다이어그램](./docs/system-design/03-erd.md)** - 엔티티 관계 모델과 테이블 구조 설명

## 🧪 테스트

프로젝트의 테스트 관련 컨벤션과 구조는 아래 문서를 참조하세요:

**[테스트 컨벤션](./docs/conventions/test-conventions.md)** - 통합 테스트 구조 및 작성 가이드라인

### 테스트 실행 방법

전체 테스트 실행:
```bash
./gradlew test
```

특정 테스트 클래스 실행:
```bash
./gradlew test --tests "kr.hhplus.be.server.api.user.UserApiTest"
```

특정 테스트 패키지 실행:
```bash
./gradlew test --tests "kr.hhplus.be.server.api.*"
```

## 모니터링 설정 (Grafana & OpenTelemetry)

이 프로젝트는 Grafana 및 OpenTelemetry를 사용한 모니터링 스택을 포함하고 있습니다.

### 구성 요소

- **Grafana**: 지표, 로그, 트레이스 시각화
- **Prometheus**: 메트릭 수집 및 저장
- **Loki**: 로그 수집 및 저장
- **Tempo**: 분산 트레이싱 데이터 저장
- **OpenTelemetry Collector**: 텔레메트리 데이터 수집 및 전송

### 실행 방법

1. Docker Compose를 사용하여 모니터링 스택 실행:
   ```bash
   docker-compose up -d
   ```

2. OpenTelemetry Java 에이전트 다운로드:
   ```bash
   # 프로젝트 루트 디렉토리에서 실행
   curl -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.3.0/opentelemetry-javaagent.jar -o opentelemetry-javaagent.jar
   ```

3. 애플리케이션 실행 (자바 에이전트 설정 포함):
   ```bash
   ./gradlew bootRun -javaagent:./opentelemetry-javaagent.jar
   ```

4. 접속 주소:
   - Grafana: http://localhost:3000 (로그인: admin/admin)
   - Prometheus: http://localhost:9090
   - 애플리케이션 메트릭: http://localhost:8080/actuator/prometheus

### Grafana 대시보드 설정

1. Grafana에 로그인 후 Configuration > Data Sources에서 다음 데이터 소스 추가:
   - Prometheus (URL: http://prometheus:9090)
   - Loki (URL: http://loki:3100)
   - Tempo (URL: http://tempo:3200)

2. 대시보드 메뉴에서 '+ Import'를 선택하고 다음 대시보드 ID를 사용하여 가져오기:
   - JVM 모니터링: 4701
   - Spring Boot: 10280

### OpenTelemetry 설정

애플리케이션은 다음 텔레메트리 데이터를 수집합니다:

- **Metrics**: Micrometer와 Prometheus를 통해 JVM 및 애플리케이션 메트릭 수집
- **Logs**: 로그 데이터는 Loki로 전송
- **Traces**: 트랜잭션 트레이스는 Tempo로 전송

자세한 설정은 `src/main/resources/application.yml` 파일을 참조하세요.
