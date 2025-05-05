# 파사드 및 애플리케이션 레이어 컨벤션

## 1. 개요

본 문서는 Voyage-Shop 애플리케이션의 파사드(Facade) 레이어와 애플리케이션(Application) 레이어의 분리 및 개발 시 일관성을 유지하고, 코드 가독성 및 유지보수성을 향상시키기 위한 규약을 정의합니다. 모든 개발자는 본 규약을 숙지하고 준수하는 것을 원칙으로 합니다.

## 2. 레이어 구조 변경 배경

기존 아키텍처에서는 파사드 패턴을 활용하여 컨트롤러와 도메인 레이어 사이의 중간 레이어로 사용했습니다. 그러나 다음과 같은 이유로 파사드 레이어와 애플리케이션 레이어를 명확히 분리하는 것이 필요하게 되었습니다:

1. **명확한 책임 분리**: 파사드는 단순 오케스트레이션에 집중하고, 복잡한 비즈니스 로직과 트랜잭션 관리는 애플리케이션 레이어에서 담당하도록 함
2. **레이어 간 경계 강화**: 각 레이어의 역할과 책임을 명확히 하여 아키텍처의 일관성 유지
3. **유지보수성 향상**: 비즈니스 로직의 변경이 파사드 레이어에 영향을 주지 않도록 설계
4. **테스트 용이성**: 각 레이어별 단위 테스트 및 통합 테스트를 쉽게 작성할 수 있도록 함

## 3. 레이어별 책임과 역할

### 3.1 파사드 레이어

#### 3.1.1 정의
파사드 레이어는 컨트롤러 레이어와 애플리케이션 레이어 사이에 위치하며, 클라이언트(컨트롤러)에게 단순화된 인터페이스를 제공하는 역할을 합니다.

#### 3.1.2 주요 책임
- 여러 애플리케이션 서비스를 조합하여 클라이언트 요청 처리
- 클라이언트 요청을 적절한 애플리케이션 서비스로 라우팅
- 간단한 데이터 변환 및 클라이언트 인터페이스 제공
- 비즈니스 로직을 직접 구현하지 않고 애플리케이션 서비스에 위임

#### 3.1.3 특징
- **가벼운 중개자**: 비즈니스 로직이나 트랜잭션 처리를 직접 수행하지 않음
- **선택적 사용**: 모든 기능에 파사드가 필요하지는 않으며, 복잡도에 따라 선별적으로 도입
- **단순 위임**: 단일 애플리케이션 서비스만 호출하는 경우 파사드 레이어는 생략 가능

### 3.2 애플리케이션 레이어

#### 3.2.1 정의
애플리케이션 레이어는 사용자의 유스케이스를 구현하고, 여러 도메인 서비스를 조합하여 비즈니스 로직을 실행하는 역할을 합니다.

#### 3.2.2 주요 책임
- 여러 도메인 서비스 조합 및 오케스트레이션
- 트랜잭션 관리 및 경계 설정
- 동시성 제어(락 메커니즘) 적용 및 관리
- 복잡한 비즈니스 프로세스 실행

#### 3.2.3 특징
- **상태 변경 관리**: 여러 도메인 엔티티의 상태 변경을 조율하고 관리
- **트랜잭션 일관성**: 여러 도메인 서비스 호출 간의 트랜잭션 일관성 보장
- **유스케이스 중심**: 사용자 요구사항에 따른 유스케이스 흐름 구현
- **비즈니스 규칙 위임**: 세부 비즈니스 규칙은 도메인 레이어에 위임

## 4. 패키지 구조

```
kr.hhplus.be.server/
  ├── controller/
  │   └── {도메인}/
  │       ├── {도메인}Controller.kt
  │       ├── {도메인}ControllerApi.kt
  │       ├── {도메인}Request.kt
  │       └── {도메인}Response.kt
  ├── facade/  
  │   └── {도메인}/
  │       ├── {도메인}Facade.kt
  │       ├── {도메인}FacadeCriteria.kt
  │       └── {도메인}FacadeResult.kt
  ├── application/
  │   └── {도메인}/
  │       ├── {도메인}Service.kt        # 애플리케이션 서비스
  │       ├── {도메인}Criteria.kt       # 요청 기준 클래스
  │       └── {도메인}Result.kt         # 응답 결과 클래스
  ├── domain/
  │   └── {도메인}/
  │       ├── {도메인}.kt               # 도메인 엔티티
  │       ├── {도메인}Repository.kt     # 리포지토리 인터페이스
  │       ├── {도메인}Service.kt        # 도메인 서비스
  │       ├── {도메인}Command.kt        # 커맨드 클래스
  │       └── {도메인}Exception.kt      # 도메인 예외
  └── infrastructure/
      └── {도메인}/
          ├── {도메인}RepositoryImpl.kt # 리포지토리 구현체
          ├── {도메인}JpaRepository.kt  # JPA 리포지토리 인터페이스
          └── {도메인}JpaEntity.kt      # JPA 엔티티
```

## 5. 파사드 레이어 컨벤션

### 5.1 기본 구조

```kotlin
@Component
class UserFacade(
    private val userService: UserService,  // 애플리케이션 서비스
    private val userPointService: UserPointService  // 필요시 다른 애플리케이션 서비스
) {
    // 파사드 메서드 구현
}
```

### 5.2 메서드 설계 원칙

1. **단순 위임**: 단일 애플리케이션 서비스만 호출하는 경우, 파사드 메서드는 그 호출을 위임하는 정도로 간결하게 구현
2. **조합 중심**: 여러 애플리케이션 서비스를 조합하는 경우에만 파사드 레이어 구현
3. **비즈니스 로직 배제**: 파사드 내에 비즈니스 로직을 직접 구현하지 않음
4. **트랜잭션 관리 지양**: 트랜잭션은 애플리케이션 서비스에서 관리하도록 하고, 파사드에서는 트랜잭션을 사용하지 않음

### 5.3 파사드 서비스 예시

```kotlin
/**
 * 사용자 관련 파사드
 * 여러 애플리케이션 서비스를 조합하여 클라이언트 요청을 처리
 */
@Component
class UserFacade(
    private val userService: UserService,
    private val userPointService: UserPointService
) {
    /**
     * 사용자 등록 및 초기 포인트 지급
     * 두 개의 애플리케이션 서비스를 조합하는 예시
     */
    fun registerUserWithInitialPoint(criteria: UserFacadeCriteria.Register): UserFacadeResult.RegisteredUser {
        // 사용자 등록
        val userCriteria = UserCriteria.Create(
            name = criteria.name,
            email = criteria.email,
            password = criteria.password
        )
        val user = userService.createUser(userCriteria)
        
        // 초기 포인트 지급
        val pointCriteria = UserPointCriteria.Create(
            userId = user.userId,
            initialAmount = 1000L
        )
        val userPoint = userPointService.createUserPoint(pointCriteria)
        
        // 결과 반환
        return UserFacadeResult.RegisteredUser(
            userId = user.userId,
            name = user.name,
            email = user.email,
            initialPoint = userPoint.amount
        )
    }
    
    /**
     * 단일 애플리케이션 서비스 호출 - 단순 위임
     * 이런 경우 파사드를 구현하는 것이 불필요할 수 있음
     */
    fun getUserProfile(criteria: UserFacadeCriteria.GetProfile): UserFacadeResult.UserProfile {
        val user = userService.getUserById(UserCriteria.GetById(userId = criteria.userId))
        return UserFacadeResult.UserProfile(
            userId = user.userId,
            name = user.name,
            email = user.email
        )
    }
}
```

### 5.4 Criteria 및 Result 클래스

```kotlin
/**
 * 파사드 레이어 요청 기준 클래스
 */
class UserFacadeCriteria {
    /**
     * 사용자 등록 요청
     */
    data class Register(
        val name: String,
        val email: String,
        val password: String
    )
    
    /**
     * 사용자 프로필 조회 요청
     */
    data class GetProfile(
        val userId: String
    )
}

/**
 * 파사드 레이어 응답 결과 클래스
 */
class UserFacadeResult {
    /**
     * 등록된 사용자 정보
     */
    data class RegisteredUser(
        val userId: String,
        val name: String,
        val email: String,
        val initialPoint: Long
    )
    
    /**
     * 사용자 프로필 정보
     */
    data class UserProfile(
        val userId: String,
        val name: String,
        val email: String
    )
}
```

## 6. 애플리케이션 레이어 컨벤션

### 6.1 기본 구조

```kotlin
@Service
class UserService(
    private val userDomainService: UserDomainService,
    private val transactionManager: PlatformTransactionManager
) {
    // 애플리케이션 서비스 메서드 구현
}
```

### 6.2 메서드 설계 원칙

1. **비즈니스 로직 위임**: 세부 비즈니스 로직은 도메인 서비스/엔티티에 위임
2. **트랜잭션 관리**: 트랜잭션 경계를 명확히 지정하고 관리
3. **동시성 제어**: 필요한 경우 분산 락 메커니즘 적용
4. **도메인 서비스 조합**: 여러 도메인 서비스를 조합하여 복잡한 유스케이스 구현
5. **예외 처리**: 도메인 예외를 포착하고 적절히 처리

### 6.3 애플리케이션 서비스 예시

```kotlin
/**
 * 사용자 관련 애플리케이션 서비스
 * 도메인 서비스를 조합하고 트랜잭션을 관리
 */
@Service
class UserService(
    private val userDomainService: kr.hhplus.be.server.domain.user.UserService,
    private val lockManager: DistributedLockManager,
    private val transactionManager: PlatformTransactionManager
) {
    /**
     * 사용자 생성
     * 트랜잭션 관리를 직접 구현한 예시
     */
    fun createUser(criteria: UserCriteria.Create): UserResult.User {
        val transactionTemplate = TransactionTemplate(transactionManager)
        
        return transactionTemplate.execute { status ->
            val createCommand = UserCommand.Create(
                name = criteria.name,
                email = criteria.email,
                password = criteria.password
            )
            
            val user = userDomainService.createUser(createCommand)
            UserResult.User.from(user)
        }
    }
    
    /**
     * 사용자 포인트 충전
     * 분산 락과 트랜잭션을 함께 사용하는 예시
     */
    fun chargeUserPoint(criteria: UserCriteria.ChargePoint): UserResult.Point {
        return lockManager.executeWithLock("user-point:${criteria.userId}") {
            val transactionTemplate = TransactionTemplate(transactionManager)
            
            transactionTemplate.execute {
                val user = userDomainService.getUser(UserCommand.GetById(criteria.userId))
                
                val chargeCommand = UserPointCommand.Charge(
                    userId = criteria.userId,
                    amount = criteria.amount
                )
                
                val chargedPoint = userPointDomainService.chargePoint(chargeCommand)
                UserResult.Point.from(chargedPoint)
            }
        }
    }
    
    /**
     * 사용자 조회
     * 읽기 전용 트랜잭션을 사용하는 예시
     */
    @Transactional(readOnly = true)
    fun getUserById(criteria: UserCriteria.GetById): UserResult.User {
        val user = userDomainService.getUser(UserCommand.GetById(criteria.userId))
        return UserResult.User.from(user)
    }
}
```

### 6.4 Criteria 및 Result 클래스

```kotlin
/**
 * 애플리케이션 레이어 요청 기준 클래스
 */
class UserCriteria {
    /**
     * 사용자 생성 요청
     */
    data class Create(
        val name: String,
        val email: String,
        val password: String
    )
    
    /**
     * 사용자 조회 요청
     */
    data class GetById(
        val userId: String
    )
    
    /**
     * 사용자 포인트 충전 요청
     */
    data class ChargePoint(
        val userId: String,
        val amount: Long
    )
}

/**
 * 애플리케이션 레이어 응답 결과 클래스
 */
class UserResult {
    /**
     * 사용자 정보
     */
    data class User(
        val userId: String,
        val name: String,
        val email: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            fun from(user: kr.hhplus.be.server.domain.user.User): User {
                return User(
                    userId = user.userId,
                    name = user.name,
                    email = user.email,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
        }
    }
    
    /**
     * 포인트 정보
     */
    data class Point(
        val userId: String,
        val amount: Long,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            fun from(point: kr.hhplus.be.server.domain.userpoint.UserPoint): Point {
                return Point(
                    userId = point.userId,
                    amount = point.amount,
                    updatedAt = point.updatedAt
                )
            }
        }
    }
}
```

## 7. 트랜잭션 관리

### 7.1 트랜잭션 책임 분리

1. **애플리케이션 레이어 책임**: 트랜잭션 관리는 오직 애플리케이션 레이어의 책임입니다.
2. **파사드 레이어의 트랜잭션 지양**: 파사드 레이어에서는 트랜잭션을 직접 관리하지 않습니다.
3. **도메인 레이어의 트랜잭션 제한**: 도메인 레이어에서는 트랜잭션을 사용할 수 있지만, 주로 애플리케이션 레이어에서 시작된 트랜잭션에 참여하는 형태로 제한합니다.

### 7.2 트랜잭션 관리 방식

애플리케이션 레이어에서는 다음 두 가지 방식으로 트랜잭션을 관리할 수 있습니다:

1. **선언적 트랜잭션 관리**:
   ```kotlin
   @Transactional
   fun createUser(criteria: UserCriteria.Create): UserResult.User {
       // 여러 도메인 서비스 호출
   }
   ```

2. **프로그래밍 방식 트랜잭션 관리**:
   ```kotlin
   fun createUser(criteria: UserCriteria.Create): UserResult.User {
       return transactionTemplate.execute { status ->
           // 여러 도메인 서비스 호출
       }
   }
   ```

### 7.3 트랜잭션 범위 최소화

트랜잭션 범위는 가능한 한 최소화하여 성능을 최적화하고 교착 상태의 위험을 줄여야 합니다:

```kotlin
// 좋은 예: 트랜잭션 범위 최소화
fun processOrder(criteria: OrderCriteria.Process): OrderResult.Order {
    // 트랜잭션 외부에서 필요한 데이터 조회
    val productInfo = productService.getProductInfo(criteria.productId)
    
    // 실제 상태 변경이 필요한 부분만 트랜잭션 적용
    return transactionTemplate.execute { status ->
        val order = orderDomainService.createOrder(OrderCommand.Create(
            userId = criteria.userId,
            productId = criteria.productId,
            quantity = criteria.quantity,
            unitPrice = productInfo.price
        ))
        
        OrderResult.Order.from(order)
    }
}
```

## 8. 동시성 제어

### 8.1 동시성 제어 책임

1. **애플리케이션 레이어 책임**: 동시성 제어(분산 락)는 애플리케이션 레이어의 책임입니다.
2. **파사드 레이어의 락 지양**: 파사드 레이어에서는 락을 직접 관리하지 않습니다.
3. **도메인 레이어의 락 제한**: 도메인 레이어에서는 Lock 관리 로직이 포함되지 않아야 합니다.

### 8.2 락 사용 패턴

다음은 애플리케이션 레이어에서 락을 사용하는 권장 패턴입니다:

```kotlin
/**
 * 락과 트랜잭션을 함께 사용하는 예시
 * 락을 먼저 획득하고 트랜잭션을 시작하는 패턴
 */
fun useUserPoint(criteria: UserCriteria.UsePoint): UserResult.Point {
    return lockManager.executeWithLock("user-point:${criteria.userId}") {
        val transactionTemplate = TransactionTemplate(transactionManager)
        
        transactionTemplate.execute {
            val userPoint = userPointDomainService.getUserPoint(
                UserPointCommand.GetByUserId(criteria.userId)
            )
            
            val usePointCommand = UserPointCommand.Use(
                userPointId = userPoint.userPointId,
                amount = criteria.amount
            )
            
            val updatedPoint = userPointDomainService.usePoint(usePointCommand)
            UserResult.Point.from(updatedPoint)
        }
    }
}
```

## 9. 단계적 리팩토링 가이드

현재 시스템에서 파사드 레이어와 애플리케이션 레이어를 분리하기 위한 단계적 리팩토링 가이드를 제시합니다.

### 9.1 준비 단계

1. **컨벤션 문서화**: 본 문서와 같이 새로운 아키텍처 컨벤션을 문서화합니다.
2. **우선순위 결정**: 복잡한 비즈니스 로직이나 트랜잭션이 필요한 기능부터 리팩토링합니다.
3. **템플릿 코드 작성**: 파사드와 애플리케이션 레이어 클래스의 템플릿 코드를 준비합니다.

### 9.2 리팩토링 실행

1. **애플리케이션 서비스 생성**: 기존 파사드 서비스의 비즈니스 로직을 새 애플리케이션 서비스로 이동합니다.
   ```kotlin
   // 기존 파사드 서비스
   @Component
   class UserFacade(
       private val userRepository: UserRepository
   ) {
       @Transactional
       fun createUser(name: String, email: String): User {
           // 비즈니스 로직 및 트랜잭션 관리
       }
   }
   
   // 새 애플리케이션 서비스
   @Service
   class UserService(
       private val userDomainService: UserDomainService
   ) {
       @Transactional
       fun createUser(criteria: UserCriteria.Create): UserResult.User {
           // 비즈니스 로직 및 트랜잭션 관리
       }
   }
   ```

2. **파사드 간소화**: 기존 파사드를 간소화하여 애플리케이션 서비스로 요청을 위임하도록 변경합니다.
   ```kotlin
   // 리팩토링 후 파사드
   @Component
   class UserFacade(
       private val userService: UserService
   ) {
       fun createUser(name: String, email: String): User {
           val criteria = UserCriteria.Create(name = name, email = email)
           return userService.createUser(criteria).toUser()
       }
   }
   ```

3. **점진적 도입**: 모든 기능을 한 번에 변경하지 말고, 기능별로 점진적으로 변경합니다.

### 9.3 테스트 및 검증

1. **단위 테스트**: 새로 작성한 애플리케이션 서비스와 파사드에 대한 단위 테스트를 작성합니다.
2. **통합 테스트**: 변경된 코드가 기존 시스템과 올바르게 통합되는지 확인합니다.
3. **코드 리뷰**: 변경사항에 대한 코드 리뷰를 통해 새 컨벤션 준수 여부를 확인합니다.

## 10. 모범 사례와 안티 패턴

### 10.1 모범 사례

1. **명확한 레이어 경계**: 각 레이어의 책임과 역할을 명확히 구분합니다.
2. **파사드 경량화**: 파사드는 단순 요청 위임과 오케스트레이션에 집중합니다.
3. **애플리케이션 서비스 특화**: 애플리케이션 서비스는 트랜잭션 관리와 도메인 서비스 조합에 집중합니다.
4. **선택적 파사드 사용**: 단순한 기능의 경우 컨트롤러에서 직접 애플리케이션 서비스를 호출할 수 있습니다.

### 10.2 안티 패턴

1. **파사드 내 비즈니스 로직**: 파사드 내에 복잡한 비즈니스 로직을 구현하는 것을 지양합니다.
2. **파사드 내 트랜잭션 관리**: 파사드에서 트랜잭션을 직접 관리하는 것을 지양합니다.
3. **애플리케이션 레이어 우회**: 컨트롤러나 파사드에서 도메인 서비스를 직접 호출하는 것을 지양합니다.
4. **중복 책임**: 파사드와 애플리케이션 서비스가 동일한 책임을 중복해서 가지는 것을 방지합니다.

## 11. 결론

파사드 레이어와 애플리케이션 레이어의 분리는 각 레이어의 책임과 역할을 명확히 하고, 코드의 유지보수성과 확장성을 향상시킵니다. 본 문서에서 정의한 컨벤션을 준수함으로써:

1. 파사드는 클라이언트에게 단순화된 인터페이스를 제공하는 역할에 집중합니다.
2. 애플리케이션 서비스는 비즈니스 로직, 트랜잭션 관리, 동시성 제어를 담당합니다.
3. 도메인 서비스는 핵심 비즈니스 규칙을 구현하고 도메인 개념을 표현합니다.

이러한 명확한 역할 분담을 통해 코드의 가독성, 유지보수성, 테스트 용이성이 향상되며, 변경에 강한 아키텍처를 구축할 수 있습니다. 