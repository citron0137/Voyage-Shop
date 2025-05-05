# 트랜잭션-락 아키텍처 마이그레이션 가이드

## 1. 개요

본 문서는 Voyage-Shop 애플리케이션에서 기존 구조를 최대한 유지하면서 트랜잭션-락 아키텍처로 마이그레이션하는 방안을 설명한다. 이 마이그레이션은 유스케이스 중심의 트랜잭션 및 락 관리를 강화하면서도 코드 베이스의 급격한 변화를 최소화하는 것을 목표로 한다.

트랜잭션-락 아키텍처에 대한 상세 내용은 다음 문서를 참조한다:
- [트랜잭션-락 아키텍처 컨벤션](./transaction-lock-architecture-convention.md)

## 2. 현재 구조 분석

### 2.1 현재 레이어 구조

현재 Voyage-Shop 프로젝트는 다음과 같은 레이어 구조로 구성되어 있다:

```
kr.hhplus.be.server/
├── controller/          # 컨트롤러 레이어
├── application/         # 애플리케이션 레이어
├── domain/              # 도메인 레이어
├── infrastructure/      # 인프라스트럭처 레이어
└── shared/              # 공유 유틸리티
    ├── lock/            # 락 관련 유틸리티
    ├── transaction/     # 트랜잭션 관련 유틸리티
    └── exception/       # 예외 처리 유틸리티
```

### 2.2 현재 책임 분배

현재 구조에서의 레이어별 책임은 다음과 같다:

1. **컨트롤러 레이어**: HTTP 요청을 받고 응답을 반환하는 역할을 담당한다. 이미 파사드 역할을 수행하고 있다.

2. **애플리케이션 레이어**: 비즈니스 로직의 조합과 트랜잭션 관리를 담당한다. 주로 `@Transactional` 어노테이션을 통해 트랜잭션을 관리하고 있다.

3. **도메인 레이어**: 핵심 비즈니스 로직과 규칙을 포함한다.

4. **인프라스트럭처 레이어**: 도메인 인터페이스의 구현체와 외부 시스템 연동을 담당한다.

5. **공유 유틸리티**: 락, 트랜잭션, 예외 처리 등의 공통 기능을 제공한다.

## 3. 마이그레이션 전략

### 3.1 마이그레이션 원칙

1. **기존 패키지 구조 유지**: 기존의 레이어드 아키텍처 패키지 구조를 유지한다.
2. **네이밍 변경 중심**: 클래스 이름 변경을 통해 책임을 명확히 한다.
3. **점진적 전환**: 중요한 도메인부터 순차적으로 변경한다.
4. **트랜잭션 관리 방식 변경**: 선언적 트랜잭션에서 명시적 트랜잭션으로 전환한다.

### 3.2 주요 변경사항

다음과 같은 주요 변경사항을 적용한다:

1. **애플리케이션 레이어 클래스 네이밍 변경**:
   - `{도메인}Service` → `{동사}{명사}UseCase`
   - 예: `OrderService` → `CreateOrderUseCase`, `GetOrderUseCase` 등

2. **트랜잭션 관리 방식 변경**:
   - `@Transactional` 어노테이션 제거
   - `TransactionTemplate`을 주입받아 명시적으로 트랜잭션 관리

3. **락 관리 추가**:
   - `DistributedLockExecutor`를 주입받아 분산 락 적용
   - 트랜잭션 시작 전에 락을 획득하고, 트랜잭션 완료 후 락 해제

4. **메서드 네이밍 통일**:
   - 유스케이스 클래스의 주요 메서드를 `execute`로 통일

## 4. 코드 마이그레이션 예시

### 4.1 기존 애플리케이션 서비스 코드

```kotlin
@Service
class OrderService(
    private val orderDomainService: OrderDomainService,
    private val productRepository: ProductRepository
) {
    @Transactional
    fun createOrder(criteria: OrderCriteria.Create): OrderResult.Order {
        val products = productRepository.findAllByIds(criteria.productIds)
        val order = orderDomainService.createOrder(
            userId = criteria.userId,
            products = products,
            shippingAddress = criteria.shippingAddress
        )
        
        return OrderResult.Order.from(order)
    }
}
```

### 4.2 마이그레이션 후 유스케이스 코드

```kotlin
@Component
class CreateOrderUseCase(
    private val orderDomainService: OrderDomainService,
    private val productRepository: ProductRepository,
    private val distributedLockExecutor: DistributedLockExecutor,
    private val transactionTemplate: TransactionTemplate
) {
    fun execute(criteria: OrderCriteria.Create): OrderResult.Order {
        val lockName = "order:create:${criteria.userId}"
        
        return distributedLockExecutor.executeWithLock(lockName) {
            transactionTemplate.execute {
                val products = productRepository.findAllByIds(criteria.productIds)
                val order = orderDomainService.createOrder(
                    userId = criteria.userId,
                    products = products,
                    shippingAddress = criteria.shippingAddress
                )
                
                OrderResult.Order.from(order)
            }
        }
    }
}
```

### 4.3 컨트롤러 코드 변경

```kotlin
// 기존 코드
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequestDto): ResponseEntity<BaseResponse<CreateOrderResponseDto>> {
        val criteria = request.toCriteria()
        val result = orderService.createOrder(criteria)
        return ResponseEntity.ok(BaseResponse.success(result.toResponseDto()))
    }
}

// 변경 후 코드
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val createOrderUseCase: CreateOrderUseCase
) {
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequestDto): ResponseEntity<BaseResponse<CreateOrderResponseDto>> {
        val criteria = request.toCriteria()
        val result = createOrderUseCase.execute(criteria)
        return ResponseEntity.ok(BaseResponse.success(result.toResponseDto()))
    }
}
```

## 5. 점진적 마이그레이션 계획

### 5.1 우선순위 도메인 선정

다음 기준으로 마이그레이션 우선순위를 결정한다:

1. **동시성 이슈가 빈번한 도메인**: 재고 관리, 결제, 포인트 트랜잭션 등
2. **비즈니스 중요도가 높은 도메인**: 주문, 결제 등 핵심 비즈니스 기능
3. **유스케이스로 분리가 용이한 도메인**: 기능이 명확히 구분되는 도메인

### 5.2 마이그레이션 단계

1. **준비 단계** (1-2주):
   - 공통 컴포넌트 설정 (DistributedLockExecutor, TransactionTemplate)
   - 팀 교육 및 가이드라인 공유

2. **파일럿 구현** (1-2주):
   - 영향도가 낮은 1-2개 도메인 선택
   - 유스케이스 패턴 적용 및 검증

3. **점진적 확장** (도메인별 1주):
   - 우선순위에 따라 도메인별로 순차적 마이그레이션
   - 각 도메인 마이그레이션 후 테스트 및 안정화

4. **완료 및 검증** (1-2주):
   - 전체 시스템 통합 테스트
   - 성능 및 안정성 검증

### 5.3 마이그레이션 관리

마이그레이션 과정에서 다음 사항을 주의깊게 관리한다:

1. **기존 코드와의 공존**: 마이그레이션 중에는 기존 서비스와 새로운 유스케이스가 공존할 수 있다.
2. **빌드 및 테스트 자동화**: CI/CD 파이프라인을 통해 마이그레이션 중 발생할 수 있는 문제를 빠르게 감지한다.
3. **롤백 계획**: 각 도메인별 마이그레이션에 대한 롤백 계획을 수립한다.
4. **문서화**: 마이그레이션된 코드에 대한 문서화를 철저히 한다.

## 6. 테스트 전략

### 6.1 단위 테스트

유스케이스 단위 테스트는 다음과 같이 구성한다:

```kotlin
@Test
fun `주문 생성 시 락과 트랜잭션이 적용되어야 한다`() {
    // given
    val criteria = OrderCriteria.Create(userId = "user1", ...)
    whenever(distributedLockExecutor.executeWithLock(any(), any())).thenAnswer {
        val action = it.getArgument<() -> Any>(1)
        action()
    }
    whenever(transactionTemplate.execute(any())).thenAnswer {
        val action = it.getArgument<TransactionCallback<Any>>(0)
        action.doInTransaction(mockTransactionStatus)
    }
    
    // when
    val result = createOrderUseCase.execute(criteria)
    
    // then
    verify(distributedLockExecutor).executeWithLock(eq("order:create:user1"), any())
    verify(transactionTemplate).execute(any())
    verify(orderDomainService).createOrder(eq("user1"), any(), any())
}
```

### 6.2 통합 테스트

통합 테스트는 다음 시나리오를 검증한다:

1. **락 획득 실패 시나리오**: 다른 프로세스가 락을 보유하고 있을 때의 동작 검증
2. **트랜잭션 롤백 시나리오**: 예외 발생 시 트랜잭션 롤백 및 락 해제 검증
3. **동시성 테스트**: 여러 스레드에서 동일 리소스에 접근할 때의 동작 검증

## 7. 결론

이 마이그레이션 가이드는 기존 레이어드 아키텍처를 유지하면서 유스케이스 중심의 트랜잭션-락 아키텍처로 전환하는 방법을 제시한다. 클래스 네이밍 변경과 책임 재배치를 통해 기존 구조를 크게 변경하지 않으면서도 트랜잭션과 락 관리를 명시적으로 개선할 수 있다.

점진적인 접근 방식을 통해 위험을 최소화하면서 새로운 아키텍처의 이점을 취할 수 있으며, 코드 품질과 비즈니스 로직의 명확성, 데이터 정합성 및 동시성 제어를 향상시킬 수 있다. 