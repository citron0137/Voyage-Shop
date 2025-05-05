# 서비스 계층의 역할 구분과 트랜잭션 관리

## 서비스 계층 구분

소프트웨어 아키텍처에서 서비스 계층은 각각 다른 책임과 역할을 가지며, 적절한 구분을 통해 유지보수성과 확장성을 확보할 수 있습니다. 여기서는 주요 서비스 계층인 도메인 서비스, 애플리케이션 서비스, 파사드 서비스의 역할과 차이점을 살펴보겠습니다.

### 도메인 서비스 (Domain Service)

**정의**: 특정 도메인 개념과 연관된 비즈니스 로직을 캡슐화하는 서비스

**특징**:
- 순수한 비즈니스 로직만을 포함
- 인프라스트럭처 의존성이 없음 (DB, 외부 시스템 등)
- 상태를 갖지 않는 무상태(stateless) 동작
- 특정 도메인 엔티티나 값 객체에 속하지 않는 연산을 처리

**책임**:
- 도메인 규칙 및 정책 구현
- 여러 엔티티에 걸친 연산 처리
- 도메인 개념 간의 조정 및 협력

**예시**:
```kotlin
class PricingService {
    fun calculateOrderPrice(order: Order, discounts: List<Discount>): Money {
        // 상품 가격 계산, 할인 적용, 세금 계산 등 비즈니스 로직
        return finalPrice
    }
}
```

### 애플리케이션 서비스 (Application Service)

**정의**: 유스케이스를 구현하고 도메인 로직의 흐름을 조정하는 서비스

**특징**:
- 트랜잭션 경계 설정
- 도메인 서비스와 인프라스트럭처 계층 사이의 조정자
- 보안, 인증, 권한 검사와 같은 기술적 관심사 처리
- 얇은 계층(thin layer)으로 유지 (최소한의 로직만 포함)

**책임**:
- 유스케이스 흐름 제어
- 트랜잭션 관리
- 입력 데이터 검증
- 도메인 객체 생성 및 조작
- 결과 변환 및 반환

**예시**:
```kotlin
@Transactional
class OrderApplicationService(
    private val orderRepository: OrderRepository,
    private val pricingService: PricingService,
    private val stockService: StockService
) {
    fun createOrder(request: CreateOrderRequest): Long {
        // 입력 검증
        validateRequest(request)
        
        // 도메인 객체 생성
        val order = Order.create(request.customerId, request.items)
        
        // 도메인 서비스 호출
        val finalPrice = pricingService.calculateOrderPrice(order, getAvailableDiscounts(request.customerId))
        order.applyPrice(finalPrice)
        
        // 재고 확인 및 예약
        stockService.reserveStock(order.items)
        
        // 저장 및 결과 반환
        return orderRepository.save(order).id
    }
}
```

### 파사드 서비스 (Facade Service)

**정의**: 복잡한 하위 시스템을 단순화된 인터페이스로 제공하는 서비스

**특징**:
- 여러 애플리케이션 서비스나 도메인 서비스를 통합
- 클라이언트(컨트롤러, 외부 시스템 등)에 단순한 인터페이스 제공
- 애플리케이션 서비스보다 더 고수준의 추상화 제공
- 데이터 변환 책임을 가짐 (DTO 변환)

**책임**:
- 여러 서비스 호출 조정
- 복잡한 워크플로우 단순화
- 입력/출력 데이터 변환 처리
- 클라이언트 요구사항에 맞는 인터페이스 제공

**예시**:
```kotlin
@Component
class OrderFacade(
    private val orderApplicationService: OrderApplicationService,
    private val customerApplicationService: CustomerApplicationService,
    private val notificationService: NotificationService
) {
    fun processOrder(request: OrderRequest): OrderResponseDTO {
        // 고객 정보 조회 및 검증
        val customer = customerApplicationService.getCustomerDetails(request.customerId)
        
        // 주문 생성
        val orderId = orderApplicationService.createOrder(
            CreateOrderRequest(
                customerId = customer.id,
                items = request.items,
                shippingAddress = customer.defaultAddress
            )
        )
        
        // 주문 정보 조회
        val order = orderApplicationService.getOrderById(orderId)
        
        // 알림 전송
        notificationService.sendOrderConfirmation(order, customer.email)
        
        // 응답 변환 및 반환
        return OrderResponseDTO.fromOrder(order)
    }
}
```

## 애플리케이션 서비스의 필요성

애플리케이션 서비스는 소프트웨어 아키텍처에서 중요한 역할을 담당하며, 다음과 같은 이유로 필요합니다:

### 1. 관심사 분리 (Separation of Concerns)

- **도메인 로직 보호**: 도메인 모델이 인프라스트럭처나 UI 관심사에 오염되지 않도록 보호
- **기술적 관심사 분리**: 트랜잭션, 보안, 로깅 등의 기술적 관심사를 비즈니스 로직과 분리
- **도메인 모델 집중**: 도메인 모델이 핵심 비즈니스 로직에만 집중할 수 있게 함

### 2. 트랜잭션 관리

- **일관된 트랜잭션 경계**: 비즈니스 작업의 원자성을 보장하는 명확한 트랜잭션 경계 제공
- **트랜잭션 격리 수준 제어**: 유스케이스에 적합한 트랜잭션 격리 수준 설정
- **분산 트랜잭션 조정**: 여러 리소스에 걸친 트랜잭션 조정 담당

### 3. 유스케이스 중심 설계

- **비즈니스 요구사항 매핑**: 각 애플리케이션 서비스 메서드가 특정 유스케이스에 직접 매핑
- **유스케이스 가시성**: 코드만으로 시스템의 유스케이스를 파악 가능
- **요구사항 변경 대응**: 유스케이스 변경 시 영향 범위를 격리하여 안전한 변경 지원

### 4. 보안 및 권한 관리

- **중앙화된 보안 정책 적용**: 모든 비즈니스 작업에 일관된 보안 정책 적용
- **세밀한 접근 제어**: 메서드 수준의 세밀한 권한 검사 구현
- **감사 로깅(Audit Logging)**: 중요 비즈니스 작업에 대한 감사 기록 생성

### 5. 통합 지점 제공

- **외부 시스템 연동**: 도메인 모델과 외부 시스템 간의 중재자 역할
- **인프라스트럭처 추상화**: 영속성, 메시징 등의 인프라스트럭처 서비스 추상화
- **이벤트 발행 및 구독**: 도메인 이벤트의 발행 및 처리 조정

## 트랜잭션과 락 관리

### 트랜잭션 관리

애플리케이션 서비스는 트랜잭션 관리의 핵심 계층으로, 다음과 같은 책임을 갖습니다:

#### 1. 트랜잭션 경계 설정

```kotlin
@Transactional
fun transferMoney(sourceAccountId: String, targetAccountId: String, amount: Money) {
    val sourceAccount = accountRepository.findById(sourceAccountId)
    val targetAccount = accountRepository.findById(targetAccountId)
    
    sourceAccount.withdraw(amount)
    targetAccount.deposit(amount)
    
    accountRepository.save(sourceAccount)
    accountRepository.save(targetAccount)
    
    eventPublisher.publish(MoneyTransferredEvent(sourceAccountId, targetAccountId, amount))
}
```

- **트랜잭션 범위**: 모든 데이터 액세스 및 비즈니스 로직을 하나의 트랜잭션으로 묶음
- **롤백 처리**: 오류 발생 시 모든 변경사항을 롤백하여 일관성 보장
- **커밋 시점 제어**: 모든 작업이 성공적으로 완료된 후에만 커밋

#### 2. 트랜잭션 속성 제어

```kotlin
@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 30)
fun processHighValueTransaction(accountId: String, amount: Money) {
    // 고액 거래 처리 로직
}

@Transactional(readOnly = true)
fun getAccountSummary(accountId: String): AccountSummaryDTO {
    // 계좌 정보 조회 로직
}
```

- **격리 수준 설정**: 유스케이스에 적합한 트랜잭션 격리 수준 지정
- **읽기 전용 트랜잭션**: 조회 작업 최적화를 위한 읽기 전용 트랜잭션
- **타임아웃 설정**: 장시간 실행되는 트랜잭션 방지

#### 3. 분산 트랜잭션 처리

```kotlin
@Transactional
fun createOrderWithPayment(orderRequest: OrderRequest) {
    // 주문 생성 (로컬 데이터베이스)
    val order = createOrder(orderRequest)
    
    try {
        // 결제 처리 (외부 시스템)
        val paymentResult = paymentService.processPayment(orderRequest.paymentDetails)
        
        // 주문 상태 업데이트
        order.confirmPayment(paymentResult.transactionId)
        orderRepository.save(order)
    } catch (e: PaymentException) {
        // 보상 트랜잭션 (Compensating Transaction)
        order.cancel("결제 실패")
        orderRepository.save(order)
        throw OrderException("결제 처리 중 오류가 발생했습니다", e)
    }
}
```

- **2단계 커밋(2PC) 대안**: 분산 시스템에서 완전한 2PC가 어려울 때 보상 트랜잭션 구현
- **Saga 패턴**: 장기 실행 트랜잭션을 위한 단계별 트랜잭션 조정
- **최종 일관성**: 즉시 일관성이 불가능한 경우 최종 일관성 보장을 위한 메커니즘 구현

### 락 관리 (Locking)

애플리케이션 서비스는 동시성 제어를 위해 다양한 락 전략을 활용합니다:

#### 1. 비관적 락 (Pessimistic Locking)

```kotlin
@Transactional
fun reserveProduct(productId: String, quantity: Int): ReservationResult {
    // 비관적 락으로 상품 데이터 잠금
    val product = productRepository.findByIdWithPessimisticLock(productId)
    
    if (product.availableStock < quantity) {
        return ReservationResult.insufficientStock()
    }
    
    product.reserveStock(quantity)
    productRepository.save(product)
    
    return ReservationResult.success(product.reservationId)
}
```

- **사용 시나리오**: 높은 경합이 예상되는 리소스, 짧은 트랜잭션
- **장점**: 충돌 방지, 데이터 일관성 강력 보장
- **단점**: 성능 저하, 교착 상태(deadlock) 가능성

#### 2. 낙관적 락 (Optimistic Locking)

```kotlin
@Transactional
fun updateProductDetails(productId: String, request: UpdateProductRequest): Product {
    val product = productRepository.findById(productId)
    
    product.updateDetails(
        name = request.name,
        description = request.description,
        price = request.price
    )
    
    try {
        return productRepository.save(product)  // 버전 충돌 시 예외 발생
    } catch (e: OptimisticLockingFailureException) {
        throw ConflictException("다른 사용자가 동시에 상품을 수정했습니다. 다시 시도해주세요.")
    }
}
```

- **사용 시나리오**: 낮은 경합 예상, 읽기 작업 많은 환경
- **장점**: 높은 동시성, 교착 상태 없음
- **단점**: 충돌 시 재시도 로직 필요, 사용자 경험 영향

#### 3. 애플리케이션 수준 락 (Application-Level Locks)

```kotlin
@Transactional
fun processUniquePayment(paymentId: String): PaymentResult {
    // 분산 락 획득 시도
    val lockAcquired = lockManager.tryLock("payment-$paymentId", 10, TimeUnit.SECONDS)
    
    if (!lockAcquired) {
        throw ConcurrentOperationException("같은 결제를 동시에 처리할 수 없습니다")
    }
    
    try {
        // 중복 결제 확인
        if (paymentRepository.existsById(paymentId)) {
            return PaymentResult.alreadyProcessed()
        }
        
        // 결제 처리 로직
        val result = processPayment(paymentId)
        return result
    } finally {
        // 항상 락 해제
        lockManager.unlock("payment-$paymentId")
    }
}
```

- **사용 시나리오**: 분산 시스템, 데이터베이스 락으로 해결 불가능한 경우
- **구현 방식**: Redis, ZooKeeper, Hazelcast 등을 활용한 분산 락
- **고려사항**: 락 획득 실패, 데드락 방지, 만료 시간 설정

#### 4. 락 사용 시 고려사항

- **락 범위 최소화**: 성능 영향을 줄이기 위해 가능한 작은 범위만 락
- **락 획득 순서 일관성**: 여러 락 필요 시 일관된 순서로 획득하여 교착 상태 방지
- **락 타임아웃 설정**: 무한정 대기 상태 방지
- **락 계층**: 애플리케이션, 서비스, 리포지터리 등 적절한 계층에서 락 관리
- **재시도 정책 구현**: 낙관적 락 충돌 시 자동 재시도 메커니즘 제공

## 결론

서비스 계층의 올바른 구분과 역할 정의는 복잡한 엔터프라이즈 애플리케이션의 유지보수성과 확장성을 크게 향상시킵니다. 특히 애플리케이션 서비스는 도메인 로직과 인프라스트럭처 사이의 중요한 조정자 역할을 담당하며, 트랜잭션과 락 관리를 통해 시스템의 데이터 일관성과 동시성 제어를 보장합니다.

효과적인 서비스 계층 설계를 위해서는:

1. 각 서비스 유형의 책임과 경계를 명확히 정의하고 유지해야 합니다.
2. 애플리케이션 서비스에 비즈니스 로직이 누출되지 않도록 주의해야 합니다.
3. 트랜잭션과 락 전략을 시스템 요구사항에 맞게 적절히 선택해야 합니다.
4. 성능과 일관성 사이의 균형을 고려한 트랜잭션 관리가 필요합니다.

이러한 원칙을 통해 복잡한 비즈니스 요구사항을 체계적으로 구현하면서도 유지보수하기 쉬운 시스템 아키텍처를 구축할 수 있습니다. 