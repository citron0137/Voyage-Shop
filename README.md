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

프로젝트는 레이어드 아키텍처 패턴을 따르며 다음과 같은 구조를 가집니다:

```
kr.hhplus.be.server/
├── controller/     # 외부 요청 처리, API 정의
├── application/    # 유스케이스 구현, 트랜잭션 경계
├── domain/         # 핵심 비즈니스 로직 및 엔티티
└── infrastructure/ # 외부 인프라 구현 (DB, 외부 API 등)
```

자세한 구조는 [코드 컨벤션](./docs/system-design/04-convention.md) 문서를 참조하세요.

## 📊 DB 구조

<img src="https://cdn-icons-png.flaticon.com/512/2906/2906274.png" width="80" alt="Database">

프로젝트의 데이터베이스 스키마 설계에 관한 문서입니다:

**[ERD 다이어그램](./docs/system-design/03-erd.md)** - 엔티티 관계 모델과 테이블 구조 설명
