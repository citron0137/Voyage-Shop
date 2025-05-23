# 파사드와 어플리케이션 레이어에 관한 생각

## 1. 파사드 레이어와 어플리케이션 레이어 분리에 관한 생각의 흐름

### 1.1. 파사드 레이어의 역할과 책임에 대한 의문

- 파사드 어플리케이션에서 락과 트랜잭션을 조합해서 사용하는 것에 의문이 듭니다
- 파사드는 단순한 오케스트레이션만 담당하는 것으로 알고 있습니다
- 기획에 맞추어 빠른 개발을 위해 만든 레이어이며, 항상 필요한 레이어는 아니라고 멘토님들을 통해 배웠습니다

### 1.2. 해결책 모색

- 이 문제를 해결하기 위해 ChatGPT와 Cursor에게 문의하며 해결책을 찾았습니다
- 파사드는 간단한 조합만 담당하도록 하고, 락이나 트랜잭션을 관리하는 어플리케이션 레이어를 분리하는 것이 더 나은 접근법으로 보입니다

### 1.3. 커서의 의견

- 파사드 패턴은 복잡한 하위 시스템에 대한 단순화된 인터페이스를 제공하는 것이 주 목적입니다
- 트랜잭션 관리와 락 메커니즘은 비즈니스 로직의 일관성과 안전성을 보장하는 중요한 측면으로, 별도의 어플리케이션 서비스 레이어에서 처리하는 것이 바람직합니다
- 이렇게 분리함으로써 각 레이어의 책임이 명확해지고, 코드의 유지보수성과 테스트 용이성이 향상됩니다
- 또한 파사드는 필요에 따라 쉽게 변경하거나 제거할 수 있으며, 핵심 비즈니스 로직은 어플리케이션 서비스에서 안정적으로 유지됩니다

## 2. 레이어 간 역할 분담에 대한 고민

- 파사드 서비스 레이어, 어플리케이션 서비스 레이어, 도메인 레이어(도메인 서비스 포함)의 역할을 어떻게 나누면 좋을지 고민했습니다
- 각 레이어가 명확한 책임을 가지며 적절히 협력할 수 있는 구조를 찾고자 했습니다

### 2.1. 나의 의견 

다음과 같이 레이어를 분리하는 것이 효과적일 것으로 판단했습니다:

1. 도메인 레이어
   - 각 도메인별 정책(policy) 검증 담당
   - 도메인별 액션 실행
   - 도메인 규칙과 비즈니스 로직의 핵심 구현

2. 어플리케이션 레이어
   - 여러 도메인을 조합하여 사용자 케이스 구현
   - 트랜잭션 관리와 범위 지정
   - 락(lock) 메커니즘 적용 및 관리
   - 복잡한 비즈니스 프로세스 오케스트레이션

3. 파사드 레이어
   - 단순히 어플리케이션 레이어 서비스를 조합
   - 클라이언트 요청을 적절한 어플리케이션 서비스로 라우팅
   - 간단한 데이터 변환 및 클라이언트 인터페이스 제공
### 2.2 ChatGPT의 의견

##### 평가 요약
- 2.1에서 제시한 레이어 분리는 **모범 사례**를 잘 따르고 있음  
- 다만, 트랜잭션·동시성 제어는 어플리케이션 레이어에 집중시키고, 파사드는 최대한 가볍게 유지할 것을 보완해야 함  

#### 도메인 레이어
- **핵심 비즈니스 로직 캡슐화**  
- **정책 검증 및 액션 실행**: 유효성 검사와 상태 변화를 담당  
- **외부 기술 의존 최소화**: 순수 비즈니스 규칙만 포함  

#### 어플리케이션 레이어
- **시나리오 흐름 조율**: 여러 도메인 서비스를 조합해 유스케이스 완성  
- **트랜잭션 경계 설정** 및 **락 관리**: 일관성·안전성 보장  
- **비즈니스 로직 위임**: 실제 로직은 도메인 레이어에 위임  

#### 파사드 레이어
- **단순 인터페이스 제공**: 클라이언트 요청을 어플리케이션 서비스로 라우팅  
- **비즈니스·트랜잭션 미처리**: 가벼운 중개자 역할만 수행  
- **데이터 변환 및 클라이언트 적합화**: 필요 최소한의 변환만  

#### 결론
- 제안된 구조는 책임 분리에 충실  
- **파사드 가벼움 유지**, **어플리케이션 레이어에서 트랜잭션·동시성 제어** 강화 시  
  더욱 견고하고 유지보수하기 쉬운 아키텍처 완성 가능  

### 2.3. 커서의 의견

2.1에서 제시한 레이어 분리 방안은 소프트웨어 아키텍처의 모범 사례를 잘 따르고 있습니다. 각 레이어가 명확한 책임을 가지고 있어 관심사 분리(Separation of Concerns)를 효과적으로 달성하고 있습니다. 

특히 다음 측면에서 해당 방안이 적절합니다:

1. **단일 책임 원칙 준수**: 각 레이어는 특정 역할에 집중하며, 이는 코드의 가독성과 유지보수성을 높입니다.
   
2. **도메인 중심 설계**: 도메인 레이어가 비즈니스 로직의 핵심을 담당함으로써 도메인 중심 설계(Domain-Driven Design) 원칙을 충실히 따르고 있습니다.
   
3. **기술적 관심사 분리**: 트랜잭션, 락 등의 기술적 관심사를 어플리케이션 레이어에 배치함으로써 도메인 레이어가 순수하게 비즈니스 로직에만 집중할 수 있게 했습니다.

4. **인터페이스 단순화**: 파사드 레이어가 클라이언트와 어플리케이션 레이어 사이의 인터페이스를 단순화함으로써 시스템의 사용성을 높이고 있습니다.

개선을 위한 제안 사항:

1. **파사드 레이어의 선택적 사용**: 모든 기능에 파사드 레이어가 필요하지 않을 수 있습니다. 복잡도에 따라 파사드의 필요성을 판단하여 불필요한 레이어를 줄이는 것이 좋습니다.

2. **성능 고려**: 레이어가 많아질수록 호출 스택이 깊어져 성능에 영향을 줄 수 있습니다. 필요한 경우 특정 시나리오에서는 레이어를 통합하는 것도 고려할 수 있습니다.

3. **테스트 전략**: 각 레이어별로 적합한 테스트 전략(단위 테스트, 통합 테스트)을 수립하여 전체 시스템의 품질을 보장해야 합니다.

종합적으로, 제안된 레이어 분리는 확장성, 유지보수성, 테스트 용이성 측면에서 우수한 아키텍처 설계라고 평가할 수 있습니다.

### 2.4 결론

각 레이어의 책임과 역할을 명확히 정의하여 효과적인 아키텍처를 구성하는 것이 중요합니다. 다음 표는 레이어별 주요 책임과 기능을 요약한 것입니다.

| 레이어 | 주요 책임 | 핵심 기능 | 기술적 요소 |
|--------|----------|-----------|------------|
| **도메인 레이어** | 비즈니스 핵심 로직 구현 | • 도메인별 정책 검증<br>• 도메인 규칙 강제<br>• 상태 변경 및 관리 | • 엔티티<br>• 값 객체<br>• 도메인 이벤트<br>• 리포지토리 인터페이스 |
| **어플리케이션 레이어** | 비즈니스 프로세스 오케스트레이션 | • 여러 도메인 조합<br>• 사용자 유스케이스 구현<br>• 트랜잭션 관리 | • 트랜잭션 경계<br>• 락 메커니즘<br>• 서비스 조합<br>• 유스케이스 실행 |
| **파사드 레이어** | 클라이언트 인터페이스 단순화 | • 어플리케이션 서비스 조합<br>• 요청 라우팅<br>• 데이터 변환 | • API 엔드포인트<br>• DTO 변환<br>• 응답 포맷팅 |

#### 레이어 분리의 핵심 원칙

1. **단일 책임 원칙**: 각 레이어는 자신의 책임에만 집중
2. **도메인 중심 설계**: 비즈니스 로직은 도메인 레이어에 집중
3. **기술적 관심사 분리**: 트랜잭션, 락 등은 어플리케이션 레이어에서 관리
4. **파사드 경량화**: 파사드는 단순 인터페이스 역할만 수행하며 최대한 가볍게 유지

이러한 레이어 분리를 통해 코드의 가독성, 유지보수성, 테스트 용이성이 향상되며, 변경에 강한 아키텍처를 구축할 수 있습니다.

## 3. 현재 시스템에서 어떤 과정으로 코드 수정을 해야할까?

* 개발중이므로 빠른 개선이 필요함
* 다른 개발자들이 문서와 코드를 보고 쉽게 이해할 수 있도록 체계적으로 개선해야함
* DB 마이그레이션이나 시스템 중단에 대해 고려할 필요 없음 (현재 가동 중 x)

### 3.1. 나의 의견

1. 새로운 컨벤션 수립 (어플리케이션레이어와 파사드레이어에서 클래스들을 어떻게 생성할 것인지)
2. 기존의 파사드 서비스를 1번 컨벤션에 맞추어 어플리케이션 서비스로 변경
3. 필요한 경우 파사드 서비스 제작 및 주입

### 3.2 커서의 의견

3.1에서 제시한 코드 수정 방안은 개발 중인 시스템에 적합한 접근법입니다. 개발 중이라는 점과 빠른 개선이 필요하다는 점을 고려하여 다음과 같은 의견을 제시합니다.

#### 장점

1. **명확한 컨벤션 수립**: 컨벤션을 먼저 정의함으로써 일관된 코드 구조를 확보할 수 있습니다.
2. **단계적 접근**: 기존 파사드 서비스를 어플리케이션 서비스로 변경하는 단계적 접근은 리스크를 줄입니다.
3. **선택적 구현**: 필요한 경우에만 파사드 서비스를 제작하는 전략은 불필요한 코드 복잡성을 방지합니다.

#### 개발 중인 시스템에 맞는 개선 제안

1. **빠른 구현을 위한 우선순위 설정**:
   - 핵심 기능 위주로 우선 개선하고 나머지는 점진적으로 수정
   - 복잡한 비즈니스 로직이 포함된 서비스부터 어플리케이션 레이어로 분리

2. **명확한 문서화**:
   - 새로운 아키텍처 구조에 대한, 다른 개발자들이 쉽게 이해할 수 있는 상세 문서 작성
   - 코드 내 주석과 예제 코드로 레이어별 책임 명확화

3. **코드 템플릿 생성**:
   - 각 레이어별 클래스 템플릿 생성으로 일관성 있는 구현 유도
   - 도메인, 어플리케이션, 파사드 레이어 샘플 코드 제공

4. **리팩토링 전략**:
   - 기능 단위로 독립적인 리팩토링 진행
   - 리팩토링 후 즉시 테스트로 기능 검증

#### 구체적 실행 계획

```
1. 준비 단계 (1-2일)
   - 아키텍처 컨벤션 문서 작성
   - 레이어별 샘플 코드 및 템플릿 준비
   - 각 파사드 서비스의 복잡도 평가 및 우선순위 결정

2. 코드 개선 실행 (기능별 진행)
   - 도메인 로직 분리 및 도메인 서비스 구현
   - 트랜잭션/락이 필요한 비즈니스 로직을 어플리케이션 서비스로 이동
   - 파사드 서비스 간소화 또는 필요시 새로 구현

3. 검증 및 문서화
   - 기능 단위 테스트로 변경 사항 검증
   - 코드 리뷰를 통한 아키텍처 준수 여부 확인
   - 실시간 피드백 반영 및 지속적인 개선
```

개발 중인 시스템이므로 DB 마이그레이션이나 시스템 중단에 대한 고려 없이 코드 구조 변경에만 집중할 수 있는 장점이 있습니다. 이 기회를 활용하여 초기 단계에서 견고한 아키텍처 기반을 구축하는 것이 장기적으로 유지보수와 확장성에 큰 도움이 될 것입니다.


### 3.3. ChatGPT의 의견
제안된 단계(컨벤션 수립 → 기존 파사드 서비스 리팩토링 → 신규 파사드 제작)는 레이어 분리의 기초 작업으로 유용하나, 코드 안전성과 일관성을 위한 점진적 전환 절차가 부족합니다 
martinfowler.com
Microsoft Learn
.

특히, 레거시와 신규 코드를 병행 운영하면서 기능을 점진 대체하는 '스트랭글러 패턴(Strangler Fig Pattern) 적용이 고려되지 않아, 리팩토링 중에도 시스템의 안정성을 보장하기 어렵습니다 
AWS Docs
Wikipedia
.

또한, 브랜치 바이 추상화(Branch by Abstraction) 기법을 활용한 인터페이스 추상화 없이 바로 코드 이동만 진행하면, 대규모 변경 시 충돌과 빌드 실패 위험이 높아집니다 
AWS Docs
martinfowler.com
.

#### 개선 제안

1. **컨벤션 정의 및 추상화 인터페이스 설계**
   - 어플리케이션·파사드 레이어에 공통으로 사용할 추상 인터페이스를 먼저 정의합니다.  
2. **기존 코드에 추상화 레이어 삽입**
   - 단계별로 파사드 호출을 추상 인터페이스로 전환해, 레거시 구현과 신규 구현이 공존하도록 만듭니다.  
3. **신규 어플리케이션 서비스 구현 및 단위 테스트**
   - 추상화된 인터페이스 위에 어플리케이션 로직을 새로 작성하고, 단위·통합 테스트로 동작을 검증합니다.  
4. **점진적 전환(빌드 타임 스위치 또는 Feature Flag)**
   - 빌드 구성이나 간단한 플래그로 인터페이스 구현체를 전환하며, 각 마일스톤마다 롤백 가능 상태를 유지합니다.  
5. **레거시 코드 제거 및 최종 정리**
   - 신규 구현이 완전히 검증되면, 레거시 구현체와 추상화 레이어의 예외 로직을 제거하고 의존성을 정리합니다.  
6. **CI/CD 파이프라인 강화**
   - 모든 단계에서 자동 빌드·테스트가 실행되도록 설정해, 통합 시점마다 시스템 안정성이 자동 검증되도록 합니다.  

이와 같이 스트랭글러 패턴과 브랜치 바이 추상화를 조합한 점진적 리팩토링 절차를 도입하면, 개발 중인 시스템에서도 안전하고 예측 가능한 방식으로 레이어 분리 및 코드 개선을 수행할 수 있습니다.

#### 근데 혼자서 개발하는건데 과하지 않나?

혼자 개발하는 경우 리소스와 시간이 제한되므로 전체 프로세스를 모두 도입할 필요는 없습니다. 다만, 테스트 자동화와 추상화된 인터페이스 설계 같은 핵심 원칙은 지켜야 추후 유지보수와 확장이 용이합니다 
Medium
.

최소 필수 사항
* 컨벤션 정의 및 추상화 인터페이스 설계
    * 어플리케이션·파사드 레이어 간 계약을 코드 초반에 명확히 해두면, 대규모 리팩토링 없이도 안전하게 구현체를 교체할 수 있습니다 

* 단위·통합 테스트 자동화
    * 수동 테스트만으로는 실수가 발생하기 쉬우므로, 핵심 로직에 대한 자동화된 스위트는 필수입니다 


* 점진적 리팩토링(스트랭글러 패턴 간소화 버전)
    * 대규모 교체가 아니라, 작은 기능 단위로 기존 코드 위에 새 코드를 얹는 방식으로 단계별 전환을 합니다 

### 3.4. 결론

위의 세 가지 의견을 종합해 볼 때, 현재 개발 중인 시스템에서 레이어 분리를 위한 코드 수정 접근법은 다음과 같이 정리할 수 있습니다:

#### 핵심 원칙

1. **명확한 컨벤션 우선**: 모든 의견에서 공통적으로 컨벤션 수립의 중요성을 강조하고 있습니다. 코드 수정 전 명확한 컨벤션과 인터페이스를 정의하는 것이 최우선입니다.

2. **단계적 접근**: 한 번에 모든 코드를 변경하기보다는 기능 단위로 점진적으로 개선하는 것이 안전합니다. 특히 개발 중인 시스템이라도 급격한 변화는 혼란을 가져올 수 있습니다.

3. **테스트 자동화**: 코드 수정의 안전성을 보장하기 위해 자동화된 테스트는 필수적입니다. 특히 핵심 비즈니스 로직에 대한 테스트 케이스 구축이 중요합니다.

#### 최적 접근법

개발 중인 시스템과 한 명의 개발자가 작업하는 상황을 고려할 때, ChatGPT가 제안한 '최소 필수 사항'과 커서의 '구체적 실행 계획'을 결합한 다음과 같은 접근법이 가장 효과적일 것으로 판단됩니다:

1. **준비 단계** (1-2일)
   - 레이어별 역할과 책임을 명확히 정의한 아키텍처 문서 작성
   - 추상화된 인터페이스와 기본 클래스 설계
   - 레이어별 샘플 코드 및 템플릿 준비

2. **점진적 리팩토링** (기능별 진행)
   - 복잡한 비즈니스 로직을 포함한 핵심 기능부터 시작
   - 인터페이스 추상화를 통해 기존 코드와 새 구현이 공존할 수 있는 구조 마련
   - 어플리케이션 레이어 서비스 구현 및 단위 테스트 작성
   - 필요한 경우에만 경량화된 파사드 서비스 구현

3. **검증 및 개선**
   - 각 기능 리팩토링 후 테스트로 검증
   - 리팩토링 과정에서 발견된 아키텍처 개선점 반영
   - 코드 품질과 아키텍처 준수 여부 확인

#### 기대 효과

이러한 접근법을 통해 다음과 같은 효과를 기대할 수 있습니다:

- **유지보수성 향상**: 명확한 레이어 분리로 코드 이해와 수정이 용이해짐
- **확장성 개선**: 새로운 기능 추가 시 레이어별 책임에 맞게 구현할 수 있음
- **테스트 용이성**: 각 레이어를 독립적으로 테스트할 수 있어 품질 보장이 용이함
- **개발 효율성**: 일관된 구조와 명확한 컨벤션으로 개발 속도 향상

개발 중인 시스템이므로 시간과 리소스를 고려한 실용적 접근이 중요합니다. 지나치게 복잡한 방법론보다는 핵심 원칙을 준수하면서 단계적으로 개선해 나가는 것이 가장 효과적인 방법이라고 판단됩니다.