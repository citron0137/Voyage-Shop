# 프로젝트 컨벤션

## Architecture 

### Layered Architecture (+ Clearn 약간)

각 레이어의 역할:
1. **Controller**: 사용자 요청을 받아 응답을 반환, API 문서화
2. **Application**: 도메인 서비스 조합, 유스케이스 구현, 트랜잭션 관리
3. **Domain**: 핵심 비즈니스 규칙과 로직 정의, 레포지토리 인터페이스 정의
4. **Infrastructure**: 외부 시스템과의 통합, 레포지토리 구현

### 레이어별 상세 규약:
- **Controller Layer**: [컨트롤러 레이어 규약](./controller-layer.md)

위와 같은 레이어 구조를 채택한 이유: [링크](./layered-architecture.md)


### Package(Folder) Structure
* Layer를 먼저 구분
* Layer마다 domain 패키지 정의
* Layer마다 Domain이 조금씩 다를 수 있음 

위와 같은 구조를 선택한 이유: [링크](./package-structure.md)

### 최종 프로젝트 구조
```
kr.hhplus.be.server/
├── controller/              # 외부 요청 처리, API 정의
│   ├── product/             # 제품 관련 컨트롤러
│   │   ├── ProductController.kt
│   │   ├── ProductRequestDTO.kt
│   │   └── ProductResponseDTO.kt
│   ├── order/               # 주문 관련 컨트롤러
│   ├── user/                # 사용자 관련 컨트롤러
│   └── ...                  # 기타 도메인 컨트롤러
│
├── application/             # 유스케이스 구현, 트랜잭션 경계
│   ├── product/             # 제품 관련 애플리케이션 서비스
│   │   ├── ProductFacade.kt  # 제품 관련 유스케이스 조율
│   │   └── ProductResult.kt  # 서비스 결과 데이터
│   ├── order/               # 주문 관련 애플리케이션 서비스
│   ├── user/                # 사용자 관련 애플리케이션 서비스
│   └── ...                  # 기타 도메인 애플리케이션 서비스
│
├── domain/                  # 핵심 비즈니스 로직 및 엔티티
│   ├── product/             # 제품 도메인
│   │   ├── Product.kt         # 제품 엔티티
│   │   ├── ProductCommand.kt  # 제품 커맨드
│   │   └── ...                # 기타 제품 도메인 관련 파일
│   ├── order/               # 주문 도메인
│   ├── user/                # 사용자 도메인
│   └── ...                  # 기타 도메인
│
├── infrastructure/          # 외부 인프라 구현 (DB, 외부 API 등)
│   ├── config/              # 인프라 관련 설정
│   ├── fake/                # 테스트용 가짜 구현체
│   └── ...                  # 기타 인프라 구현
│
├── config/                  # 전체 애플리케이션 설정
├── shared/                  # 공통 유틸리티 코드
└── ServerApplication.kt     # 애플리케이션 진입점
```


## 린트
추후 적용 예정