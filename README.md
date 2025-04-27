# Voyage Shop

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

## 📑 목차

- [소개](#소개)
- [프로젝트 문서](#프로젝트-문서)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [테스트](#테스트)
- [모니터링](#모니터링)
- [성능 최적화](#성능-최적화)
- [개선 포인트](#개선-포인트)

## 📌 소개

**Voyage Shop**은 온라인 쇼핑몰 백엔드 서비스입니다. 제품 관리, 주문 처리, 사용자 관리, 쿠폰 및 포인트 시스템을 포함하고 있습니다.

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
   git clone https://github.com/citron0137/voyage-shop.git
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
src/main/kotlin/kr/hhplus/be/server/
  ├── controller/     # API 엔드포인트 및 요청 처리
  ├── application/    # 비즈니스 유스케이스 구현
  ├── domain/         # 핵심 비즈니스 로직 및 규칙
  └── infrastructure/ # 외부 시스템 연동 및 DB 접근
```

자세한 설계 원칙과 규약은 [프로젝트 컨벤션](./docs/conventions/01.common-conventions.md) 문서를 참조하세요.

### 📏 개발 규약 (Conventions)

개발 과정에서 준수해야 할 규칙과 패턴을 정의합니다:

- [프로젝트 컨벤션 개요](./docs/conventions/01.common-conventions.md) - 전체 컨벤션 요약 및 링크
- [레이어드 아키텍처](./docs/conventions/03.layered-architecture.md) - 아키텍처 선택 이유 및 구조
- [도메인 레이어 규약](./docs/conventions/07.domain-layer.md) - 핵심 비즈니스 로직 작성 규칙

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

## 📊 모니터링

시스템 성능과 안정성을 모니터링하기 위한 방법을 설명합니다.

- [조회 성능 모니터링 구축 방안](docs/monitoring/06-performance-monitoring.md)

## ⚡️ 성능 최적화

현재 문서들은 데이터 조회 성능 최적화에 중점을 두고 있습니다. 대규모 데이터셋에서 효율적인 데이터 검색 및 조회 방법을 다루고 있으며, 향후 쓰기 성능 및 트랜잭션 처리 최적화에 대한 가이드가 추가될 예정입니다.

### 성능 개선 방안
- [성능 이슈 분석 개요](docs/performance/01-performance-issues.md)
- [주문 아이템 순위 조회 성능 개선 방안](docs/performance/02-order-rank-performance-solution.md)
- [주문 목록 조회 성능 개선 방안](docs/performance/03-order-list-performance-solution.md)
- [쿠폰 사용자 및 상품 조회 성능 개선 방안](docs/performance/04-coupon-user-product-performance-solution.md)
- [락(Lock)을 사용하는 조회 기능 성능 개선 방안](docs/performance/05-lock-performance-solution.md)

### 성능 테스트
- [부하 테스트 방안](docs/performance/07-load-testing.md)

## 🔍 개선 포인트

### 코드 품질 개선
**도메인 중심 검증**: Command DTO를 POJO 객체로 유지하고 생성 시 검증을 도메인 객체가 담당하도록 책임 이전, 이를 통해 도메인 로직의 일관성 및 캡슐화 강화

### 성능 및 인프라
**서비스 모니터링 방안 구축**: 서비스 상태, 성능 지표, 사용자 행동 패턴을 실시간으로 추적하고 분석할 수 있는 통합 모니터링 시스템 구축. 데이터 수집 및 시각화를 통한 서비스 안정성 및 성능 개선 기반 마련

**성능 테스트 방안 구축**: 다양한 부하 상황에서 시스템의 응답성과 확장성을 검증할 수 있는 체계적인 성능 테스트 프레임워크 도입. 임계값 설정을 통한 성능 저하 조기 감지 및 병목 구간 식별 자동화

**성능 개선 및 시스템 확장**: 데이터 처리량 증가에 따른 시스템 확장성 확보를 위해 캐싱 전략 최적화, 비동기 처리 도입 및 수평적 확장이 가능한 아키텍처로 전환. 주요 트랜잭션 경로에 대한 성능 개선을 통해 응답 시간 단축 및 처리량 증대

<!--
현재 프로젝트의 향후 개선 방향과 발전 가능성을 설명합니다.

- **확장성 개선**: 마이크로서비스 아키텍처로의 전환 검토
- **보안 강화**: 인증/인가 메커니즘 개선 및 데이터 암호화 적용
- **국제화 지원**: 다국어 및 다중 통화 지원
- **분산 시스템 도입**: 대용량 트래픽 처리를 위한 분산 시스템 구성
- **API 버전 관리**: 효과적인 API 버전 관리 전략 도입
- **실시간 데이터 처리**: 이벤트 기반 아키텍처 도입 검토
- **서비스 품질 모니터링**: 사용자 경험 중심의 모니터링 체계 구축
-->
