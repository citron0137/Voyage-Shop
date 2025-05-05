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
- [스크립트 사용 설명서](#스크립트-사용-설명서)
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

### 간단한 실행 방법

프로젝트를 가장 빠르게 실행하는 방법은 제공된 도커 스크립트를 사용하는 것입니다:

#### Linux/Mac 환경
```bash
# 프로젝트 루트 디렉토리에서 실행
./run-docker.sh up -d
```

#### Windows 환경
```powershell
# 프로젝트 루트 디렉토리에서 실행
.\run-docker.ps1 up -d
```

이 명령은 필요한 모든 컨테이너(MySQL, 애플리케이션 등)를 백그라운드에서 실행합니다.
실행 후 브라우저에서 `http://localhost:8080`으로 접속하여 API를 사용할 수 있습니다.
Swagger UI는 `http://localhost:8080/swagger-ui/index.html#/`에서 확인할 수 있습니다.

## 스크립트 사용 설명서

Voyage Shop은 개발 및 테스트 프로세스를 간소화하기 위한 다양한 스크립트를 제공합니다.

### 1. 도커 환경 관리 스크립트

#### Windows 환경
```powershell
.\run-docker.ps1 [명령어]
```

#### Linux/Mac 환경
```bash
./run-docker.sh [명령어]
```

#### 주요 명령어
- `up -d`: 모든 도커 컨테이너를 백그라운드에서 실행
- `clean`: 모든 컨테이너와 볼륨 데이터 삭제
- `clean -a`: 모든 컨테이너, 이미지, 볼륨, 네트워크 등 전체 시스템 정리

### 2. SQL 실행 스크립트

#### Windows 환경
```powershell
.\run-sql.ps1 [옵션] <SQL파일경로>
```

#### Linux/Mac 환경
```bash
./run-sql.sh [옵션] <SQL파일경로>
```

#### 주요 옵션
- `-Service` / `-s, --service`: MySQL 서비스 이름 (기본값: mysql)
- `-User` / `-u, --user`: MySQL 사용자 이름 (기본값: root)
- `-Password` / `-p, --password`: MySQL 비밀번호 (기본값: root)
- `-Database` / `-d, --database`: 데이터베이스 이름 (기본값: hhplus)
- `-Charset` / `--charset`: 문자셋 (기본값: utf8mb4)
- `-Help` / `-h, --help`: 도움말 표시

### 3. 부하 테스트 스크립트

#### Windows 환경
```powershell
.\run-loadtest.ps1 [명령어] [옵션]
```

#### Linux/Mac 환경
```bash
./run-loadtest.sh [명령어] [옵션]
```

#### 주요 명령어
- `start`: 테스트 환경 시작 (애플리케이션, DB, Redis, 모니터링 도구)
- `stop`: 테스트 환경 중지 및 삭제
- `data-gen`: 테스트 데이터 생성
  - `-js`: JS 스크립트로 데이터 생성
  - `-pg`: PostgreSQL용 데이터 생성
  - `-sql`: SQL 스크립트로 데이터 생성
  - `-size=[숫자]`: SQL 스크립트 사용 시 데이터 크기 (1000, 10000, 100000)
- `test`: 부하 테스트 실행
  - `-basic`: 기본 성능 테스트 (기본값)
  - `-cache`: 캐시 효율성 테스트
  - `-spike`: 트래픽 스파이크 테스트
- `help`: 도움말 표시

#### 예시
```
.\run-loadtest.ps1 start       # 테스트 환경 시작
.\run-loadtest.ps1 data-gen -sql -size=10000  # 10,000개 주문 SQL 데이터 생성
.\run-loadtest.ps1 test -cache # 캐시 효율성 테스트 실행
```

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

자세한 테스트 방법 및 구조는 [테스트 컨벤션](./docs/coventions-for-test/01.test-conventions.md) 문서를 참조하세요.

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
