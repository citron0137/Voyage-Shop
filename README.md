# Voyage Shop

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

## 목차

- [소개](#소개)
- [프로젝트 문서](#프로젝트-문서)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [테스트](#테스트)
- [모니터링](#모니터링)
- [성능 최적화](#성능-최적화)
- [개선 포인트](#개선-포인트)

## 소개

**Voyage Shop**은 온라인 쇼핑몰 백엔드 서비스입니다. 제품 관리, 주문 처리, 사용자 관리, 쿠폰 및 포인트 시스템을 포함하고 있습니다.

## 프로젝트 문서

프로젝트에 대한 상세 문서는 다음 카테고리로 구분됩니다.

### 요구사항 문서 (Requirements)

비즈니스 요구사항과 기능 명세를 담고 있습니다:

- [기능 요구사항](./docs/requirements/01-functional-requirements.md) - 주요 기능 목록
- [도메인 요구사항](./docs/requirements/02-domain-requirements.md) - 도메인 규칙 및 제약조건
- [비기능 요구사항](./docs/requirements/03-non-functional-requirements.md) - 성능, 보안 등 품질 요구사항

### 설계 문서 (Design)

시스템 설계와 아키텍처에 관한 문서입니다:

- [시퀀스 다이어그램](./docs/design/sequence-diagram.md) - 주요 기능 흐름도
- [데이터베이스 설계](./docs/design/database-design.md) - DB 스키마 및 테이블 구조

## 기술 스택

Voyage Shop은 다음 기술 스택을 사용하여 개발되었습니다.

| 분류 | 기술 |
|------|------|
| **언어** | Kotlin |
| **프레임워크** | Spring Boot |
| **빌드 도구** | Gradle |
| **컨테이너화** | Docker |
| **데이터베이스** | MySQL |

## 시작하기

### 사전 준비

프로젝트를 실행하기 위해 다음 도구가 필요합니다:

- JDK 17 이상
- Docker 및 Docker Compose 또는 Kubernetes(kubectl, k8s 클러스터)

### 실행 방법

프로젝트를 실행하는 여러 방법이 있습니다. 필요에 따라 선택하세요:

#### 방법 A: Docker Compose 사용 (권장)

> **이 방법의 장점**: 단일 명령으로 전체 스택을 쉽게 설정할 수 있으며, 개발과 테스트에 일관된 환경을 제공합니다. 별도의 빌드 단계가 필요 없고, 애플리케이션과 데이터베이스가 자동으로 연결됩니다.

```bash
# MySQL과 애플리케이션 모두 실행
docker compose -f docker-compose.yml -f docker-compose.app.yml up -d
```

필요한 경우 특정 버전을 지정할 수도 있습니다:

```bash
# Git 커밋 해시를 버전으로 사용하는 예
docker build -t voyage-shop-app --build-arg VERSION=$(git rev-parse --short HEAD) .
docker compose -f docker-compose.yml -f docker-compose.app.yml up -d
```

#### 방법 B: Kubernetes 사용

> **이 방법의 장점**: 프로덕션 환경과 유사한 설정으로 애플리케이션을 실행하고 테스트할 수 있습니다. 확장성, 자동 복구, 롤링 업데이트 등 쿠버네티스의 다양한 기능을 활용할 수 있으며, 대규모 배포에 적합합니다.

```bash
# 도커 이미지 빌드
docker build -t voyage-shop:latest .

# 쿠버네티스에 배포
kubectl apply -k k8s/
```

> 자세한 Kubernetes 설정은 [k8s/README.md](./k8s/README.md)를 참조하세요.

#### 방법 C: 로컬 환경에서 직접 실행

> **이 방법의 장점**: 개발 중에 코드를 빠르게 변경하고 즉시 결과를 확인할 수 있습니다. IDE의 디버깅 기능을 활용할 수 있고, 빌드 과정이 간소화되어 반복적인 개발과 테스트에 최적화되어 있습니다.

```bash
# MySQL만 도커로 실행
docker compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

브라우저에서 `http://localhost:8080`으로 접속하여 API를 사용할 수 있습니다.

## 프로젝트 구조

프로젝트는 레이어드 아키텍처 패턴을 따릅니다. 주요 패키지 구조는 다음과 같습니다:

```
src/main/kotlin/kr/hhplus/be/server/
  ├── controller/     # API 엔드포인트 및 요청 처리
  ├── application/    # 비즈니스 유스케이스 구현
  ├── domain/         # 핵심 비즈니스 로직 및 규칙
  └── infrastructure/ # 외부 시스템 연동 및 DB 접근
```

자세한 설계 원칙과 규약은 [프로젝트 컨벤션](./docs/conventions/01.common-conventions.md) 문서를 참조하세요.

### 개발 규약 (Conventions)

개발 과정에서 준수해야 할 규칙과 패턴을 정의합니다:

- [프로젝트 컨벤션 개요](./docs/conventions/01.common-conventions.md) - 전체 컨벤션 요약 및 링크
- [레이어드 아키텍처](./docs/conventions/03.layered-architecture.md) - 아키텍처 선택 이유 및 구조
- [도메인 레이어 규약](./docs/conventions/07.domain-layer.md) - 핵심 비즈니스 로직 작성 규칙

## 테스트

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

## 모니터링

시스템 성능과 안정성을 모니터링하기 위한 방법을 설명합니다.

- [조회 성능 모니터링 구축 방안](docs/monitoring/06-performance-monitoring.md)

## 성능 최적화

현재 문서들은 데이터 조회 성능 최적화에 중점을 두고 있습니다. 대규모 데이터셋에서 효율적인 데이터 검색 및 조회 방법을 다루고 있으며, 향후 쓰기 성능 및 트랜잭션 처리 최적화에 대한 가이드가 추가될 예정입니다.

### 성능 개선 방안
- [성능 이슈 분석 개요](docs/performance/01-performance-issues.md)
- [주문 아이템 순위 조회 성능 개선 방안](docs/performance/02-order-rank-performance-solution.md)
- [주문 목록 조회 성능 개선 방안](docs/performance/03-order-list-performance-solution.md)
- [쿠폰 사용자 및 상품 조회 성능 개선 방안](docs/performance/04-coupon-user-product-performance-solution.md)
- [락(Lock)을 사용하는 조회 기능 성능 개선 방안](docs/performance/05-lock-performance-solution.md)

### 성능 테스트
- [부하 테스트 방안](docs/performance/07-load-testing.md)

## 개선 포인트

프로젝트 개선 방향에 대한 상세 내용은 이슈를 참고해주세요.  
[Voyage Shop 이슈 페이지](https://github.com/citron0137/voyage-shop/issues)
