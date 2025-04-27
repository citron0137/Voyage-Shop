# Voyage Shop

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

## 📑 목차

- [소개](#소개)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [테스트](#테스트)
- [성능 최적화 가이드](#성능-최적화-가이드)
- [프로젝트 문서](#프로젝트-문서)

## 📌 소개

**Voyage Shop**은 온라인 쇼핑몰 백엔드 서비스입니다. 제품 관리, 주문 처리, 사용자 관리, 쿠폰 및 포인트 시스템을 포함하고 있습니다.

## 🛠 기술 스택

Voyage Shop은 다음 기술 스택을 사용하여 개발되었습니다.

| 분류 | 기술 |
|------|------|
| **언어** | Kotlin |
| **프레임워크** | Spring Boot |
| **빌드 도구** | Gradle |
| **컨테이너화** | Docker |
| **데이터베이스** | MySQL |

## 🚀 시작하기

### 사전 준비

프로젝트를 실행하기 위해 다음 도구가 필요합니다:

- JDK 17 이상
- Docker 및 Docker Compose

### 환경 설정

1. **저장소 클론**

   ```bash
   git clone https://github.com/your-username/voyage-shop.git
   cd voyage-shop
   ```

2. **도커 컨테이너 실행**

   ```bash
   docker-compose up -d
   ```

3. **애플리케이션 실행**

   ```bash
   ./gradlew bootRun
   ```

## 📂 프로젝트 구조

프로젝트는 레이어드 아키텍처 패턴을 따릅니다. 주요 패키지 구조는 다음과 같습니다:

```
src/
  ├── controller/     # API 엔드포인트 및 요청 처리
  ├── application/    # 비즈니스 유스케이스 구현
  ├── domain/         # 핵심 비즈니스 로직 및 규칙
  └── infrastructure/ # 외부 시스템 연동 및 DB 접근
```

자세한 설계 원칙과 규약은 [프로젝트 컨벤션](./docs/conventions/01.common-conventions.md) 문서를 참조하세요.

## 🧪 테스트

프로젝트는 다양한 레벨의 테스트를 포함하고 있습니다:

- 도메인 레이어 유닛 테스트
- 애플리케이션 레이어 유닛/통합 테스트
- 컨트롤러 레이어 API 테스트
- 동시성 테스트

자세한 테스트 방법 및 구조는 [테스트 컨벤션](./docs/conventions/09.test-conventions.md) 문서를 참조하세요.

### 테스트 실행 방법

**전체 테스트 실행:**
```bash
./gradlew test
```

**특정 테스트 클래스 실행:**
```bash
./gradlew test --tests "kr.hhplus.be.server.api.user.UserApiTest"
```

**특정 테스트 패키지 실행:**
```bash
./gradlew test --tests "kr.hhplus.be.server.api.*"
```

## ⚡️ 성능 최적화 가이드

현재 문서들은 데이터 조회 성능 최적화에 중점을 두고 있습니다. 대규모 데이터셋에서 효율적인 데이터 검색 및 조회 방법을 다루고 있으며, 향후 쓰기 성능 및 트랜잭션 처리 최적화에 대한 가이드가 추가될 예정입니다.

- [성능 이슈 분석 개요](docs/performance/01-performance-issues.md)
- [주문 아이템 순위 조회 성능 개선 방안](docs/performance/02-order-rank-performance-solution.md)
- [주문 목록 조회 성능 개선 방안](docs/performance/03-order-list-performance-solution.md)
- [쿠폰 사용자 및 상품 조회 성능 개선 방안](docs/performance/04-coupon-user-product-performance-solution.md)
- [락(Lock)을 사용하는 조회 기능 성능 개선 방안](docs/performance/05-lock-performance-solution.md)
- [조회 성능 모니터링 구축 방안](docs/performance/06-performance-monitoring.md)
- [부하 테스트 방안](docs/performance/07-load-testing.md)

## 📋 프로젝트 문서

프로젝트에 대한 상세 문서는 다음 카테고리로 구분됩니다.

### ✅ 요구사항 문서 (Requirements)

비즈니스 요구사항과 기능 명세를 담고 있습니다:

- [기능 요구사항](./docs/requirements/01-functional-requirements.md) - 주요 기능 목록
- [도메인 요구사항](./docs/requirements/02-domain-requirements.md) - 도메인 규칙 및 제약조건
- [비기능 요구사항](./docs/requirements/03-non-functional-requirements.md) - 성능, 보안 등 품질 요구사항

### 📐 설계 문서 (Design)

시스템 설계와 아키텍처에 관한 문서입니다:

- [시퀀스 다이어그램](./docs/design/sequence-diagram.md) - 주요 기능 흐름도
- [데이터베이스 설계](./docs/design/database-design.md) - DB 스키마 및 테이블 구조

### 📏 개발 규약 (Conventions)

개발 과정에서 준수해야 할 규칙과 패턴을 정의합니다:

- [프로젝트 컨벤션 개요](./docs/conventions/01.common-conventions.md) - 전체 컨벤션 요약 및 링크
- [레이어드 아키텍처](./docs/conventions/03.layered-architecture.md) - 아키텍처 선택 이유 및 구조
- [도메인 레이어 규약](./docs/conventions/07.domain-layer.md) - 핵심 비즈니스 로직 작성 규칙
