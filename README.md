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
- [성능 최적화 가이드](#성능-최적화-가이드)


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

## 성능 최적화 가이드

현재 문서들은 데이터 조회 성능 최적화에 중점을 두고 있습니다. 대규모 데이터셋에서 효율적인 데이터 검색 및 조회 방법을 다루고 있으며, 향후 쓰기 성능 및 트랜잭션 처리 최적화에 대한 가이드가 추가될 예정입니다.

- [성능 이슈 분석 개요](docs/performance/01-performance-issues.md)
- [주문 아이템 순위 조회 성능 개선 방안](docs/performance/02-order-rank-performance-solution.md)
- [주문 목록 조회 성능 개선 방안](docs/performance/03-order-list-performance-solution.md)
- [쿠폰 사용자 및 상품 조회 성능 개선 방안](docs/performance/04-coupon-user-product-performance-solution.md)
- [락(Lock)을 사용하는 조회 기능 성능 개선 방안](docs/performance/05-lock-performance-solution.md)
- [조회 성능 모니터링 구축 방안](docs/performance/06-performance-monitoring.md)
- [부하 테스트 방안](docs/performance/07-load-testing.md)
