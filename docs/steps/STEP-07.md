# 7단계: JPA 리포지토리 구현 및 통합 테스트

## 작업 개요

이 브랜치(`feat/jpa-repository-implementation`)에서는 도메인 모델과 데이터베이스를 연결하는 **JPA 리포지토리** 구현과 **통합 테스트**를 추가했습니다. 또한 데이터베이스 마이그레이션을 위한 **Flyway** 설정 및 SQL 파일을 구성했습니다.

---

## 주요 작업 내용

### 데이터베이스 스키마 관리 (Flyway)

- Flyway 데이터베이스 마이그레이션 설정 
- 사용자 테이블 생성 SQL 추가
- 제품 테이블 생성 SQL 추가
- 쿠폰 사용자 테이블 및 통합 테스트 추가
- 쿠폰 이벤트 테이블 및 통합 테스트 추가
- 주문 및 결제 테이블 생성 SQL 추가

### JPA 리포지토리 구현

- JPA 리포지토리 구현체 추가 및 폴더 구조 정리
- 상품 조회 시 **동시성 제어**를 위한 락 기능 추가
- 유저 포인트 JPA 레포지토리 구현
- 사용자 포인트 관련 코드 구현

### 통합 테스트 추가

- 사용자 등록 통합 테스트 추가 및 데이터 소스 설정
- 사용자 포인트 흐름 통합 테스트 추가
- 포인트 충전에 대한 **동시성 테스트** 코드 추가
- 상품 서비스 통합 테스트 추가 및 수정
- ProductFacade 통합 테스트 추가
- 주문 파사드 통합 테스트 구현

### 문서화 및 테스트 규약

- README 및 문서 업데이트, 테스트 관련 문서 추가
- 테스트 컨벤션 문서 업데이트 및 구조 개선
- 통합 테스트 관련 가이드라인 추가
- 테스트 메서드 네이밍 규칙 업데이트
- 자세한 내용은 [테스트 컨벤션 문서](../conventions/test-conventions.md) 참조

---

## 커밋 히스토리

### 테스트 컨벤션 정의
- [fdc8557](https://github.com/citron0137/Voyage-Shop/commit/fdc8557e4961922272669ee083bd1420427a6510) **테스트 컨벤션 정의**
- [dbd092c](https://github.com/citron0137/Voyage-Shop/commit/dbd092c2db1015fba331537571ad5207ce1422b0) **테스트 가이드라인**
- [a8a18fe](https://github.com/citron0137/Voyage-Shop/commit/a8a18fe4e698a258aa2e8399ea5b2f0bfe5a56e9) **네이밍 규칙 정의**


### 공통/인프라
- [b965514](https://github.com/citron0137/Voyage-Shop/commit/b965514f8598895c7ab58074d98956050836407e) **Flyway 설정**
- [c1faba9](https://github.com/citron0137/Voyage-Shop/commit/c1faba99a40d8721fc71fa2f55bb961fcd0b97cd) **기본 문서 추가**


### 리포지터리 인터페이스 구현 
- [6f4dbe5](https://github.com/citron0137/Voyage-Shop/commit/6f4dbe5485375c7225b28d457acf675e9cfbbb6f) **JPA 리포지토리 구현**

### 사용자 관련
- [b965514](https://github.com/citron0137/Voyage-Shop/commit/b965514f8598895c7ab58074d98956050836407e) **사용자 및 포인트 테이블 생성**
- [feeb6a1](https://github.com/citron0137/Voyage-Shop/commit/feeb6a1795d95ce19dab7fffa8d3c1a5e9127a02) **사용자 등록 테스트**

### 상품 관련
- [0c1aa67](https://github.com/citron0137/Voyage-Shop/commit/0c1aa67ed89f3f69a9baddf505e84832630de296) **제품 테이블 생성**
- [aea4ca7](https://github.com/citron0137/Voyage-Shop/commit/aea4ca74b70f1bf54e4a2dcd2b3e6022d9092d8e) **비관적 락 적용**
- [f566338](https://github.com/citron0137/Voyage-Shop/commit/f5663382b509918c125a5f84eee940f06f0ceecf) **상품 서비스 테스트**
- [539f083](https://github.com/citron0137/Voyage-Shop/commit/539f0836d72576cebf32184fa2205e7b840fdecf) **임포트 오류 수정**
- [a950747](https://github.com/citron0137/Voyage-Shop/commit/a95074755a88b74f520ebfd273b2d9e4fd7b8555) **파사드 테스트**
- [203082d](https://github.com/citron0137/Voyage-Shop/commit/203082de768ac88f8624f0d563524c544892ad6f) **테스트 수정**

### 포인트 관련
- [48519f7](https://github.com/citron0137/Voyage-Shop/commit/48519f7569b5f897b1373f548269e1206c3ee7cf) **포인트 레포지토리 락 추가**
- [d11a194](https://github.com/citron0137/Voyage-Shop/commit/d11a194f76c32ca6c89e150821fc26b9891e9216) **포인트 서비스 락 추가**
- [244aff7](https://github.com/citron0137/Voyage-Shop/commit/244aff7422e2d476b61c0d0ecb035b86f8a6289e) **포인트 흐름 테스트**
- [a2b2a98](https://github.com/citron0137/Voyage-Shop/commit/a2b2a985d17e2f82c2705f783a45eec1a0761b65) **동시성 테스트**

### 쿠폰 관련
- [9274e58](https://github.com/citron0137/Voyage-Shop/commit/9274e58fcd72832d383d00b2f0930b5cb569b120) **쿠폰 사용자 테이블 추가**
- [aec705f](https://github.com/citron0137/Voyage-Shop/commit/aec705f0928292475107121a94ebfb6fd3ce8e10) **쿠폰 이벤트 테이블 추가**

### 주문/결제 관련
- [d022660](https://github.com/citron0137/Voyage-Shop/commit/d022660365476ab8c037ec5670efc3987484867b) **주문/결제 테이블 생성 및 테스트**

---

## 주요 기술적 결정

### 1. **Flyway를 사용한 데이터베이스 마이그레이션**
   - 버전 관리 및 팀 협업 효율화를 위해 **Flyway**를 도입
   - **SQL 파일 기반**의 마이그레이션으로 직관적인 관리

### 2. **테스트 컨벤션 개선**
   - **영어 메서드명 + 한글 DisplayName** 형식 표준화
   - 통합 테스트 구조화 및 패턴 정립
   - 자세한 내용은 [테스트 컨벤션 문서](../conventions/test-conventions.md) 참조

### 3. **비관적 락(Pessimistic Lock) 구현**
   - 동시성 제어가 필요한 리소스에 **비관적 락** 적용
   - **`@Lock(LockModeType.PESSIMISTIC_WRITE)`** 어노테이션 활용
   - 포인트 충전, 쿠폰 재고 감소 등의 **동시성 테스트**에서 발생하는 실패를 해결하기 위해 도입
   - 특히 동시에 여러 요청이 몰릴 수 있는 리소스에 대한 테스트가 일관되게 통과할 수 있도록 구현
   - 이는 **임시적인 방편**으로, 추후 **분산 락**이나 **낙관적 락** 등 더 효율적인 방식으로 개선 예정
   - 성능 테스트 후 실제 서비스 부하를 고려한 최적의 동시성 제어 전략 적용 계획


---

## 다음 단계

1. 동시성 관련 이슈들 더 찾아서 수정하기
2. 성능의 문제가 발생할 수 있는 부분 파악하고 미리 대처하기