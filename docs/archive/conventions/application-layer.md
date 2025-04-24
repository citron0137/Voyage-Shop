# 애플리케이션 레이어 규약

## 1. 개요

본 문서는 Voyage-Shop 애플리케이션의 애플리케이션(Application) 레이어 개발 시 일관성을 유지하고, 코드 가독성 및 유지보수성을 향상시키기 위한 규약을 정의합니다. 모든 개발자는 본 규약을 숙지하고 준수하는 것을 원칙으로 합니다.

애플리케이션 레이어의 주요 역할은 여러 도메인 서비스를 조합하여 사용자 요구사항을 구현하는 것입니다. 이 레이어는 도메인 로직을 직접 구현하지 않고, 도메인 레이어에 정의된 서비스를 적절히 호출하고 조합하는 오케스트레이션 역할에 집중해야 합니다. 또한 트랜잭션 경계를 정의하고 관리하는 책임도 갖습니다.

## 2. 디렉토리 구조

```
/application
  /도메인명/
    DomainFacade.kt    # 도메인 퍼사드 클래스
    DomainCriteria.kt  # 요청 기준 클래스
    DomainResult.kt    # 응답 결과 클래스
```

각 도메인별로 독립된 패키지를 생성하고, 그 안에 관련 퍼사드, 요청 기준, 응답 결과 클래스를 배치합니다.

## 3. 퍼사드 패턴 (XXXFacade)

### 3.1 기본 구조

```kotlin
@Component
class DomainFacade(
    private val domainService: DomainService,
    private val otherService: OtherService
) {
    // 메서드 구현
}
```

### 3.2 퍼사드 설계 원칙

1. **단일 책임**: 각 퍼사드는 특정 도메인에 대한 유스케이스 조합을 담당
2. **트랜잭션 경계**: 필요한 경우 `@Transactional` 어노테이션을 사용하여 트랜잭션 경계 설정
3. **의존성 주입**: 생성자 주입을 통해 필요한 서비스 의존성 주입
4. **예외 문서화**: 발생 가능한 예외를 KDoc 주석으로 명시적으로 문서화
5. **자세한 주석**: 각 메서드의 기능과 매개변수, 반환 값에 대한 자세한 설명 제공
6. **도메인 로직 배제**: 애플리케이션 레이어에는 복잡한 도메인 로직이 포함되지 않아야 함. 이 레이어는 단순히 도메인 서비스들을 조합하고 오케스트레이션하는 역할만 수행해야 하며, 실질적인 비즈니스 로직은 도메인 레이어에 위치해야 함

예시:
```kotlin
/**
 * 새로운 사용자를 생성합니다.
 * 사용자 생성과 함께 해당 사용자의 포인트 정보도 함께 생성합니다.
 *
 * @param criteria 사용자 생성 요청 기준
 * @return 생성된 사용자 정보
 * @throws UserPointException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
 * @throws RuntimeException 사용자 생성 또는 포인트 생성 과정에서 예기치 않은 오류가 발생한 경우
 */
@Transactional
fun createUser(criteria: UserCriteria.Create = UserCriteria.Create()): UserResult.User {
    // 사용자 생성
    val createdUser = userService.createUser()
    
    // 사용자 포인트 생성
    val createPointCommand = UserPointCommand.Create(userId = createdUser.userId)
    userPointService.create(createPointCommand)
    
    return UserResult.User.from(createdUser)
}
```

## 4. 요청 기준 클래스 (XXXCriteria)

### 4.1 기본 구조

```kotlin
class DomainCriteria {
    // 도메인별 요청 기준 클래스 정의
    data class Operation(
        // 요청 파라미터 정의
    ) {
        // 필요 시 검증 로직 추가
    }
}
```

### 4.2 요청 기준 설계 원칙

1. **중첩 클래스**: 각 도메인의 작업별로 중첩 클래스로 구분
2. **불변 객체**: 요청 기준 객체는 불변(immutable)으로 설계
3. **기본값 제공**: 필요한 경우 파라미터에 기본값 제공
4. **유효성 검증**: 생성자 내에서 기본적인 유효성 검증 수행
5. **명확한 네이밍**: 작업 의도가 명확히 드러나는 클래스 이름 사용
6. **Command 변환 메소드**: 각 Criteria 클래스는 도메인 Command 객체로 변환해주는 메소드를 제공해야 함

예시:
```kotlin
/**
 * 사용자 관련 요청 기준을 담는 클래스
 */
class UserCriteria {
    /**
     * 사용자 생성 요청
     */
    class Create {
        /**
         * 도메인 Command로 변환
         */
        fun toCommand(): UserCommand.Create {
            return UserCommand.Create()
        }
    }

    /**
     * 사용자 조회 요청
     */
    data class GetById(
        val userId: String
    ) {
        init {
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
        }
    }

    /**
     * 모든 사용자 조회 요청
     */
    class GetAll
}
```

### 4.3 도메인 객체 변환 규칙

1. **toCommand 패턴**: 모든 Criteria 클래스는 `toCommand()` 메소드를 통해 도메인 Command 객체로 변환되어야 함
2. **책임 분리**: 도메인 간 변환 로직은 Criteria 내부에 캡슐화하여, Facade 클래스는 변환 세부 로직을 알 필요가 없도록 설계
3. **파라미터 전달**: Criteria 클래스는 필요한 정보를 매개변수로 받아 내부에서 처리하는 방식으로 구현
4. **명시적 변환**: 여러 도메인 간 변환이 필요한 경우 (예: BenefitMethod -> CouponBenefitMethod) 해당 변환 로직을 Criteria의 toCommand 메소드 내부에 구현하여 캡슐화

```kotlin
/**
 * 쿠폰 발급 요청
 */
data class IssueCoupon(
    val couponEventId: String,
    val userId: String
) {
    /**
     * CouponUserCommand로 변환
     * 
     * @param benefitMethod 쿠폰 이벤트의 혜택 방식
     * @param benefitAmount 혜택 금액
     * @return 생성된 CouponUserCommand.Create 객체
     */
    fun toCommand(benefitMethod: BenefitMethod, benefitAmount: String): CouponUserCommand.Create {
        // BenefitMethod를 CouponBenefitMethod로 변환
        val couponBenefitMethod = when (benefitMethod) {
            BenefitMethod.DISCOUNT_FIXED_AMOUNT -> CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT
            BenefitMethod.DISCOUNT_PERCENTAGE -> CouponBenefitMethod.DISCOUNT_PERCENTAGE
        }
        
        return CouponUserCommand.Create(
            userId = userId,
            benefitMethod = couponBenefitMethod,
            benefitAmount = benefitAmount
        )
    }
}
```

### 4.4 검증 책임 분배

애플리케이션 레이어에서의 검증은 다음 원칙에 따라 구현합니다:

1. **최소한의 검증**: 애플리케이션 레이어에서는 기본적인 데이터 형식 검증만 수행하고, 비즈니스 규칙 검증은 도메인 레이어에 위임
2. **단순 DTO**: Criteria 클래스는 가능한 한 단순 DTO로 유지하고, 복잡한 검증 로직은 피함
3. **검증 책임 분리**: 
   - 애플리케이션 레이어: 형식 검증(빈 값, 기본 형식 등)
   - 도메인 레이어: 비즈니스 규칙 검증, 도메인 로직 검증
4. **Facade에서의 검증**: 유스케이스 수준의 검증(예: 두 Criteria 간의 관계 검증)은 Facade 메소드에서 수행

예시:
```kotlin
// 도메인 레이어에서의 비즈니스 규칙 검증
class CreateCouponEventCommand(
    val benefitMethod: BenefitMethod,
    val benefitAmount: String,
    val totalIssueAmount: Long
) {
    init {
        require(totalIssueAmount > 0) { "총 발급 수량은 0보다 커야 합니다." }
        validateBenefitAmount()
    }
    
    private fun validateBenefitAmount() {
        // 도메인 규칙에 따른 검증
    }
}

// 애플리케이션 레이어의 Facade에서의 유스케이스 검증
@Transactional
fun issueCouponToUser(issueCriteria: CouponEventCriteria.IssueCoupon, userCriteria: UserCriteria.GetById): CouponEventResult.IssueCoupon {
    // 두 Criteria 간의 관계 검증 (유스케이스 수준 검증)
    if (isUserEligibleForCoupon(issueCriteria.couponEventId, userCriteria.userId)) {
        // 로직 수행
    } else {
        throw IneligibleUserException("This user is not eligible for this coupon")
    }
}
```

## 5. 응답 결과 클래스 (XXXResult)

### 5.1 기본 구조

```kotlin
class DomainResult {
    // 단일 항목 응답
    data class Single(
        // 필드 정의
    ) {
        companion object {
            fun from(domainEntity: DomainEntity): Single {
                // 변환 로직
            }
        }
    }

    // 목록 응답
    data class List(
        val items: kotlin.collections.List<Single>
    ) {
        companion object {
            fun from(entities: kotlin.collections.List<DomainEntity>): List {
                // 변환 로직
            }
        }
    }
}
```

### 5.2 응답 결과 설계 원칙

1. **중첩 클래스 활용**: 응답 타입별로 중첩 클래스 사용
2. **도메인 모델 변환**: 도메인 모델을 애플리케이션 결과 객체로 변환하는 `from` 메서드 제공
3. **불변 객체 사용**: 모든 응답 결과는 불변 객체로 설계
4. **타입 안전성**: 명확한 타입 정의로 컴파일 타임에 오류 검출 가능
5. **네임스페이스 구분**: 도메인 모델과 이름 충돌 방지를 위한 패키지 구조 활용
6. **도메인 객체 생성자**: 각 Result 클래스는 도메인 객체를 파라미터로 받아 생성하는 생성자 또는 정적 팩토리 메소드를 제공해야 함
7. **일관된 네이밍**: 단일 항목 응답은 `Single`로, 목록 응답은 `List`로 네이밍

예시:
```kotlin
/**
 * 사용자 관련 응답 결과를 담는 클래스
 */
class UserResult {
    /**
     * 단일 사용자 정보 응답
     */
    data class Single(
        val userId: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            // 도메인 객체로부터 Result 객체 생성
            fun from(user: kr.hhplus.be.server.domain.user.User): Single {
                return Single(
                    userId = user.userId,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
        }

        // 생성자를 통한 변환 방식도 가능
        // constructor(user: kr.hhplus.be.server.domain.user.User) : this(
        //     userId = user.userId,
        //     createdAt = user.createdAt,
        //     updatedAt = user.updatedAt
        // )
    }

    /**
     * 사용자 목록 응답
     */
    data class List(
        val users: kotlin.collections.List<Single>
    ) {
        companion object {
            fun from(users: kotlin.collections.List<kr.hhplus.be.server.domain.user.User>): List {
                return List(
                    users = users.map { Single.from(it) }
                )
            }
        }
    }
}
```

## 6. 트랜잭션 관리

### 6.1 트랜잭션 경계 설정

트랜잭션 경계는 애플리케이션 레이어에서 정의하는 것을 원칙으로 합니다.

```kotlin
@Transactional
fun createOrder(criteria: OrderCriteria.Create): OrderResult.Order {
    // 여러 도메인 서비스 호출
}
```

### 6.2 트랜잭션 관리 원칙

1. **트랜잭션 경계**: 여러 도메인 서비스를 조합하는 작업은 항상 트랜잭션으로 관리
2. **읽기 전용 트랜잭션**: 조회 작업은 가능한 경우 `@Transactional(readOnly = true)` 적용
3. **트랜잭션 전파**: 필요한 경우 명시적인 전파 옵션 설정 (기본값: `REQUIRED`)
4. **트랜잭션 격리 수준**: 필요한 경우 명시적인 격리 수준 설정

## 7. 예외 처리

### 7.1 예외 처리 전략

1. **도메인 예외 전파**: 도메인 레이어에서 발생한 예외를 최대한 그대로 전파
2. **예외 변환 지양**: 의미 있는 예외는 가능한 변환하지 않고 그대로 전달
3. **문서화**: 발생 가능한 예외를 메서드 주석에 명시적으로 문서화

### 7.2 예외 처리 예시

```kotlin
/**
 * 사용자 포인트를 충전합니다.
 *
 * @param criteria 포인트 충전 요청 기준
 * @return 충전된 포인트 정보
 * @throws UserPointException.UserNotFound 사용자를 찾을 수 없는 경우
 * @throws UserPointException.AmountShouldPositive 충전 금액이 양수가 아닌 경우
 */
@Transactional
fun chargePoint(criteria: UserPointCriteria.Charge): UserPointResult.Point {
    val command = UserPointCommand.Charge(
        userId = criteria.userId,
        amount = criteria.amount
    )
    
    val chargedPoint = userPointService.charge(command)
    return UserPointResult.Point.from(chargedPoint)
}
```

## 8. 테스트

### 8.1 테스트 대상

애플리케이션 레이어 테스트는 다음 두 수준으로 작성합니다:

1. **단위 테스트**: 퍼사드 클래스의 각 메서드에 대한 단위 테스트 (서비스 계층을 Mocking)
2. **통합 테스트**: 실제 서비스 구현체를 사용한 통합 테스트 (또는 경량 컨테이너 테스트)

### 8.2 테스트 네이밍 규칙

- `[메서드명]_[시나리오]_[기대결과]`

예시:
```kotlin
@Test
fun createUser_success_returnsCreatedUser() { ... }

@Test
fun findUserById_userNotExists_throwsUserNotFoundException() { ... }
```

### 8.3 테스트 원칙

1. **독립적 테스트**: 각 테스트는 독립적으로 실행 가능해야 함
2. **가독성**: 테스트 코드는 가독성을 최우선으로 작성
3. **경계 조건 테스트**: 정상 케이스뿐만 아니라 경계 조건과 예외 케이스도 테스트
4. **트랜잭션 테스트**: 트랜잭션 동작을 검증하는 테스트 케이스 포함 