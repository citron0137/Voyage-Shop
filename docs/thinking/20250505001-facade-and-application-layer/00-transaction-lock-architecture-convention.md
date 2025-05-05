# 9. 트랜잭션-락 아키텍처 컨벤션

## 1. 개요

본 문서는 Voyage-Shop 애플리케이션의 트랜잭션과 락 관리를 위한 아키텍처 컨벤션을 정의한다. 레이어드 아키텍처에서 유스케이스(Application Layer)가 락과 트랜잭션을 직접 관리하도록 구조화하고, 파사드 레이어는 오케스트레이션 및 응답 조립 역할만 담당하는 방식을 설명한다.

아키텍처 개요는 다음 문서들을 참조한다:
- [1. 프로젝트 컨벤션 개요](../conventions/01.common-conventions.md)
- [3. 레이어드 아키텍처](../conventions/03.layered-architecture.md)
- [6. 애플리케이션 레이어 규약](../conventions/06.application-layer.md)

## 2. 아키텍처 구조

### 2.1 레이어 구조와 책임

본 아키텍처는 다음 레이어로 구성된다:

1. **파사드 레이어 (Facade Layer)**
   - 외부 시스템(API)과의 인터페이스 역할을 담당한다
   - 오케스트레이션 및 응답 조립만 담당한다
   - 트랜잭션이나 락을 직접 관리하지 않는다

2. **애플리케이션 레이어 (Application Layer, UseCase)**
   - 비즈니스 유스케이스를 구현한다
   - **트랜잭션과 락 관리의 주체**이다
   - 도메인 서비스 호출 및 조정을 담당한다

3. **도메인 레이어 (Domain Layer)**
   - 핵심 비즈니스 로직 및 규칙을 포함한다
   - 트랜잭션이나 락에 대한 개념이 없다
   - 순수한 도메인 로직에 집중한다

### 2.2 락과 트랜잭션 위치의 근거

유스케이스 내에서 락과 트랜잭션을 관리해야 하는 이유는 다음과 같다:

1. **단일 책임 원칙**
   - 파사드는 외부 시스템과의 통신에 집중한다
   - 유스케이스는 비즈니스 로직 및 데이터 정합성에 집중한다

2. **정합성 보장**
   - 비즈니스 로직과 트랜잭션 경계가 동일한 레이어에 위치하여 정합성을 보장한다
   - 락과 트랜잭션이 유스케이스 단위로 관리되어 일관된 데이터 상태를 유지한다

3. **테스트 용이성**
   - 유스케이스 단위로 트랜잭션과 락을 모킹하여 테스트가 가능하다
   - 파사드는 더 가볍게 테스트가 가능하다

## 3. 트랜잭션과 락 관리 규칙

### 3.1 실행 순서

모든 유스케이스에서 다음 순서를 준수해야 한다:

```
락 획득 → 트랜잭션 시작 → 비즈니스 로직 실행 → 트랜잭션 커밋 → 락 해제
```

이 순서는 데이터 정합성과 동시성 문제를 효과적으로 해결한다.

### 3.2 유틸리티 사용

유스케이스에서는 다음 유틸리티를 활용한다:

1. **DistributedLockExecutor**
   - 분산 환경에서 동시성 제어를 위한 락 관리를 담당한다
   - 유스케이스 실행 전체를 감싸는 외부 레이어로 작동한다

2. **TransactionTemplate**
   - 스프링의 선언적 트랜잭션 관리를 담당한다
   - 락 내부에서 트랜잭션을 실행한다

### 3.3 기본 패턴

```kotlin
fun execute(request: RequestDto): ResponseDto {
    return distributedLockExecutor.executeWithLock(getLockName(request)) {
        transactionTemplate.execute {
            // 비즈니스 로직 실행
            // 도메인 서비스 호출
            // 응답 생성
        }
    }
}
```

## 4. 구현 예시

### 4.1 주문 생성 유스케이스

```kotlin
@Component
class CreateOrderUseCase(
    private val distributedLockExecutor: DistributedLockExecutor,
    private val transactionTemplate: TransactionTemplate,
    private val orderDomainService: OrderDomainService,
    private val productRepository: ProductRepository
) {
    fun execute(request: CreateOrderRequestDto): CreateOrderResponseDto {
        val lockName = "order:create:${request.userId}"
        
        return distributedLockExecutor.executeWithLock(lockName) {
            transactionTemplate.execute {
                // 1. 도메인 객체 및 서비스 호출
                val products = productRepository.findAllByIds(request.productIds)
                val order = orderDomainService.createOrder(
                    userId = request.userId,
                    products = products,
                    shippingAddress = request.shippingAddress
                )
                
                // 2. 응답 DTO 변환 및 반환
                CreateOrderResponseDto(
                    orderId = order.id,
                    totalAmount = order.totalAmount,
                    orderStatus = order.status.name,
                    estimatedDeliveryDate = order.estimatedDeliveryDate
                )
            }
        }
    }
}
```

### 4.2 재고 업데이트 유스케이스

```kotlin
@Component
class UpdateInventoryUseCase(
    private val distributedLockExecutor: DistributedLockExecutor,
    private val transactionTemplate: TransactionTemplate,
    private val inventoryDomainService: InventoryDomainService
) {
    fun execute(request: UpdateInventoryRequestDto): UpdateInventoryResponseDto {
        val lockName = "inventory:update:${request.productId}"
        
        return distributedLockExecutor.executeWithLock(lockName) {
            transactionTemplate.execute {
                // 비즈니스 로직 실행
                val updatedInventory = inventoryDomainService.updateInventory(
                    productId = request.productId,
                    quantityChange = request.quantityChange,
                    updateReason = request.reason
                )
                
                // 응답 생성
                UpdateInventoryResponseDto(
                    productId = updatedInventory.productId,
                    currentQuantity = updatedInventory.quantity,
                    updatedAt = updatedInventory.updatedAt
                )
            }
        }
    }
}
```

## 5. 파사드 레이어 지침

### 5.1 역할과 책임

파사드 레이어는 다음과 같은 역할을 담당한다:
- 외부 요청을 수신하고 응답을 반환한다
- 유스케이스 호출 및 응답 조립을 수행한다
- 오류 처리 및 로깅을 담당한다
- 입력 유효성 검사를 수행한다

파사드 레이어는 절대 다음을 수행하지 않는다:
- 트랜잭션을 시작하거나 관리하지 않는다
- 락을 획득하거나 관리하지 않는다
- 복잡한 비즈니스 로직을 포함하지 않는다

### 5.2 파사드 예시 코드

```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderFacade(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getOrderUseCase: GetOrderUseCase
) {
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequestDto): ResponseEntity<CreateOrderResponseDto> {
        val response = createOrderUseCase.execute(request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<GetOrderResponseDto> {
        val response = getOrderUseCase.execute(GetOrderRequestDto(orderId))
        return ResponseEntity.ok(response)
    }
}
```

## 6. 디렉토리 구조와 네이밍 컨벤션

### 6.1 디렉토리 구조

```
kr.hhplus.be.server/
├── facade/                  # 파사드 레이어
│   ├── order/
│   │   ├── OrderFacade.kt
│   │   ├── dto/
│   │       ├── request/
│   │       └── response/
│   └── product/
│
├── application/             # 애플리케이션 레이어 (유스케이스)
│   ├── order/
│   │   ├── CreateOrderUseCase.kt
│   │   ├── GetOrderUseCase.kt
│   │   └── dto/
│   └── product/
│
├── domain/                  # 도메인 레이어
│   ├── order/
│   │   ├── Order.kt
│   │   ├── OrderDomainService.kt
│   │   └── OrderRepository.kt
│   └── product/
│
└── infrastructure/          # 인프라 레이어
    ├── config/
    │   ├── TransactionConfig.kt
    │   └── LockConfig.kt
    └── persistence/
```

### 6.2 네이밍 컨벤션

1. **파사드 클래스**
   - `{도메인}Facade`: OrderFacade, ProductFacade

2. **유스케이스 클래스**
   - `{동사}{명사}UseCase`: CreateOrderUseCase, UpdateInventoryUseCase

3. **DTO 클래스**
   - 요청: `{유스케이스명}RequestDto`: CreateOrderRequestDto
   - 응답: `{유스케이스명}ResponseDto`: CreateOrderResponseDto

4. **도메인 서비스**
   - `{도메인}DomainService`: OrderDomainService, ProductDomainService

## 7. 도입 효과

### 7.1 데이터 정합성 강화

- 트랜잭션과 락의 책임이 명확하게 유스케이스에 부여된다
- 비즈니스 로직과 데이터 조작의 경계가 일치하여 일관성이 보장된다
- 분산 환경에서도 동시성 이슈가 최소화된다

### 7.2 관심사 분리

- 파사드: API 계약 및 요청/응답 처리에 집중한다
- 유스케이스: 비즈니스 로직 및 트랜잭션 관리에 집중한다
- 도메인 서비스: 핵심 비즈니스 규칙에 집중한다

### 7.3 테스트 용이성

- 각 레이어별 독립적인 테스트가 가능하다
- 트랜잭션과 락을 모킹하여 유스케이스 테스트가 간소화된다
- 도메인 로직은 순수 함수로 테스트가 가능하다

### 7.4 코드 가독성 및 유지보수성

- 명확한 책임 분리로 코드베이스 파악이 용이하다
- 동일한 패턴 적용으로 새로운 기능 개발 시 일관성이 유지된다
- 비즈니스 로직의 변경 시 영향 범위가 최소화된다 