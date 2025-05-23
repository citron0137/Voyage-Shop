# 도메인 레이어 컨벤션

이 문서는 도메인 레이어에서 사용하는 컨벤션을 정의합니다. 도메인 레이어는 핵심 비즈니스 로직을 담당하는 계층으로, DDD(Domain-Driven Design) 원칙을 따릅니다.

## 목차

1. [도메인 엔티티](#1-도메인-엔티티)
2. [도메인 서비스](#2-도메인-서비스)
3. [리포지토리 인터페이스](#3-리포지토리-인터페이스)
4. [커맨드 패턴](#4-커맨드-패턴)
5. [예외 처리](#5-예외-처리)
6. [값 객체](#6-값-객체)

## 1. 도메인 엔티티

### 1.1 정의

도메인 엔티티는 비즈니스 개념을 표현하는 객체로, 식별자(ID)를 통해 구분됩니다. 엔티티는 자신의 생명주기를 관리하고, 자신의 비즈니스 규칙을 캡슐화합니다.

### 1.2 명명 규칙

- 클래스 이름은 명사로 시작하며, 단수형을 사용합니다. (예: `User`, `Product`, `Order`)
- 엔티티의 식별자는 `{엔티티명}Id` 형식을 사용합니다. (예: `userId`, `productId`, `orderId`)
- 시간 관련 필드는 `createdAt`, `updatedAt` 등 일관된 형식을 사용합니다.

### 1.3 구조

```kotlin
data class User(
    val userId: String,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // 비즈니스 로직을 메서드로 구현
    fun validatePassword(password: String): Boolean {
        // 비즈니스 로직
    }
    
    // 불변성을 유지하면서 상태 변경하는 메서드
    fun updateEmail(newEmail: String): User {
        return this.copy(
            email = newEmail,
            updatedAt = LocalDateTime.now()
        )
    }
}
```

### 1.4 원칙

- 엔티티는 불변(immutable)으로 설계합니다. 상태 변경은 새 객체를 반환하는 메서드로 구현합니다.
- 비즈니스 로직은 엔티티 내부에 메서드로 구현합니다.
- 엔티티 간의 관계는 ID를 통해 표현합니다.
- 비즈니스 규칙 위반 시 적절한 도메인 예외를 발생시킵니다.

## 2. 도메인 서비스

### 2.1 정의

도메인 서비스는 특정 엔티티에 속하지 않는 비즈니스 로직을 구현하거나, 여러 엔티티 간의 조합 작업을 수행합니다.

### 2.2 명명 규칙

- 클래스 이름은 `{도메인}Service` 형식을 사용합니다. (예: `UserService`, `OrderService`)
- 메서드 이름은 동사 또는 동사구로 시작합니다. (예: `createUser`, `placeOrder`, `calculateDiscount`)

### 2.3 구조

```kotlin
@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val stockCalculator: StockCalculator
) {
    fun getProduct(command: ProductCommand.GetById): Product {
        return productRepository.findById(command.productId)
            ?: throw ProductException.NotFound("Product with id: ${command.productId}")
    }
    
    fun decreaseStock(command: ProductCommand.DecreaseStock): Product {
        // 각 메서드는 직접 리포지토리에 접근
        val product = productRepository.findById(command.productId)
            ?: throw ProductException.NotFound("Product with id: ${command.productId}")
        
        if (product.stock < command.amount) {
            throw ProductException.StockAmountUnderflow(
                "Stock amount underflow for product: ${command.productId}"
            )
        }
        
        val updatedProduct = product.copy(
            stock = product.stock - command.amount,
            updatedAt = LocalDateTime.now()
        )
        
        return productRepository.update(updatedProduct)
    }
}
```

### 2.4 원칙

- 도메인 서비스는 상태를 가지지 않는 것을 권장합니다.
- 트랜잭션 관리는 애플리케이션 레이어에서 담당합니다.
- 도메인 서비스는 리포지토리를 통해 영속성 레이어와 통신합니다.
- 커맨드 객체를 통해 요청을 받고 처리합니다.
- **중복 메서드를 절대 만들지 않습니다.** 동일한 기능을 수행하는 메서드는 하나만 존재해야 합니다.
- 메서드 이름은 그 역할을 명확히 표현해야 합니다. 단순히 내부 구현을 드러내는 이름(예: `process`, `handle`)보다는 비즈니스 의도를 드러내는 이름(예: `issueCoupon`, `calculateDiscount`)을 사용합니다.
- 메서드의 문서화(JavaDoc)를 통해 목적, 매개변수, 반환 값, 예외 등을 명확하게 설명합니다.
- **메서드 간 직접 호출을 지양합니다.** 각 메서드는 자신의 작업을 수행하기 위해 필요한 리포지토리에 직접 접근해야 합니다. 다른 서비스 메서드를 호출하면 코드의 복잡성이 증가하고 의존성이 깊어져 테스트와 유지보수가 어려워집니다.

### 2.5 메서드 설계 가이드라인

- **강력한 중복 방지 원칙**: 절대로 다음과 같은 중복 메서드를 만들지 않습니다:
  - 단순히 다른 메서드를 호출만 하는 메서드 (예: `getCouponUserWithValidation` → `getCouponUser`)
  - 이름만 다르고 동일한 기능을 하는 메서드 (예: `use` → `useCoupon`) 
  - 파라미터나 반환 타입이 동일하고 내부 구현이 거의 같은 메서드

- **내부 헬퍼 메서드는 private으로 제한**: 내부 구현 세부사항을 다루는 헬퍼 메서드는 `private`으로 선언하여 외부로 노출되지 않도록 합니다.

- **메서드 통합 우선**: 기능이 유사한 메서드는 통합하고, 필요에 따라 내부 로직을 분기처리하는 것이 좋습니다.

- **단일 책임 원칙 준수**: 각 메서드는 하나의 명확한 책임만 가져야 합니다.

- **애매한 네이밍 금지**: `process`, `handle`, `execute`와 같은 구체적이지 않은 이름은 지양합니다. 대신 `issueCoupon`, `calculateDiscount`, `validateOrder`와 같이 기능을 명확히 설명하는 이름을 사용합니다.

- **독립적인 메서드 설계**: 각 메서드는 다른 서비스 메서드에 의존하지 않고 독립적으로 동작해야 합니다. 공통 로직이 필요한 경우, 헬퍼 메서드를 사용하여 중복을 줄이되 메서드 간 직접 호출은 피합니다.

#### 좋은 예시:

```kotlin
// 좋은 예시: 명확한 이름으로 하나의 메서드만 제공
fun issueCoupon(command: CouponCommand.Create): Coupon {
    // 쿠폰 발급 로직
}

// 좋은 예시: 내부 구현은 private 메서드로 분리
fun getProduct(command: ProductCommand.GetById): Product {
    validateProductId(command.productId)
    return repository.findById(command.productId)
        ?: throw ProductException.NotFound("Product with id: ${command.productId}")
}

private fun validateProductId(productId: String) {
    if (productId.isBlank()) {
        throw ProductException.InvalidId("Product ID cannot be blank")
    }
}

// 좋은 예시: 각 메서드가 독립적으로 리포지토리에 접근
fun useCoupon(command: CouponUserCommand.Use): CouponUser {
    // 다른 서비스 메서드 호출 대신 직접 리포지토리 접근
    val couponUser = repository.findById(command.couponUserId)
        ?: throw CouponException.NotFound("Coupon with id: ${command.couponUserId}")
        
    val usedCouponUser = couponUser.use()
    return repository.update(usedCouponUser)
}
```

#### 절대 하지 말아야 할 나쁜 예시:

```kotlin
// 나쁜 예시 1: 유사한 기능을 수행하는 중복 메서드
fun use(command: CouponCommand.Use): Coupon {
    // 쿠폰 사용 로직
}

fun useCoupon(command: CouponCommand.Use): Coupon {
    return use(command) // 단순히 다른 메서드 호출
}

// 나쁜 예시 2: 단순 위임만 하는 메서드
fun getCouponUser(command: CouponUserCommand.GetById): CouponUser {
    return repository.findById(command.couponUserId)
        ?: throw CouponException.NotFound("Coupon with id: ${command.couponUserId}")
}

// 아래 메서드는 완전히 불필요함!
fun getCouponUserWithValidation(command: CouponUserCommand.GetById): CouponUser {
    return getCouponUser(command) // 단순 위임만 수행
}

// 나쁜 예시 3: 애매한 이름의 메서드
fun process(command: OrderCommand.Process): Order {
    // 어떤 처리를 하는지 이름만으로는 명확하지 않음
}

// 나쁜 예시 4: 메서드 간 직접 호출
fun calculateDiscountAmount(command: CouponUserCommand.CalculateDiscount): Long {
    // 다른 서비스 메서드를 호출하여 의존성 발생
    val couponUser = getCouponUser(CouponUserCommand.GetById(command.couponUserId))
    
    return couponUser.calculateDiscountAmount(command.originalAmount)
}
```

위 나쁜 예시들은 코드의 복잡성을 증가시키고 유지보수를 어렵게 만듭니다. 항상 메서드 하나가 명확한 하나의 책임을 가지도록 설계하고, 불필요한 중복을 제거하며, 메서드 간 직접 호출을 지양하는 것이 중요합니다.

## 3. 리포지토리 인터페이스

### 3.1 정의

리포지토리는 도메인 객체의 영속성을 담당하는 인터페이스입니다. 도메인 레이어에서는 리포지토리 인터페이스만 정의하고, 구현체는 인프라스트럭처 레이어에 위치합니다.

### 3.2 명명 규칙

- 인터페이스 이름은 `{도메인}Repository` 형식을 사용합니다. (예: `UserRepository`, `OrderRepository`)
- 메서드 이름은 `findById`, `findByUserId`, `create`, `update`, `delete` 등 일관된 명명을 사용합니다.

### 3.3 구조

```kotlin
interface ProductRepository {
    fun findById(productId: String): Product?
    fun findAll(): List<Product>
    fun create(product: Product): Product
    fun update(product: Product): Product
    fun delete(productId: String)
    fun findByCategory(categoryId: String): List<Product>
}
```

### 3.4 원칙

- 리포지토리는 데이터베이스 접근 세부 사항을 숨깁니다.
- 리포지토리 메서드는 도메인 객체와 ID만을 파라미터로 사용합니다.
- 페이징, 정렬 등의 기능은 객체 파라미터로 전달합니다.
- 리포지토리는 도메인 객체만 반환하며, DTO를 반환하지 않습니다.

## 4. 커맨드 패턴

### 4.1 정의

커맨드 패턴은 요청을 객체로 캡슐화하여 요청에 필요한 모든 정보를 포함하고, 검증 로직을 중앙화하며, 메서드 호출을 일관성 있게 만드는 데 도움이 됩니다.

### 4.2 명명 규칙

- 커맨드 클래스는 `{도메인}Command` 형식으로 명명합니다. (예: `UserCommand`, `OrderCommand`)
- 내부 클래스는 수행할 작업을 명확히 표현합니다. (예: `GetById`, `Create`, `Update`, `Delete` 등)

### 4.3 기본 구조

```kotlin
sealed class ProductCommand {
    data class GetById(val productId: String) : ProductCommand() {
        init {
            if (productId.isBlank()) {
                throw ProductException.ProductIdShouldNotBlank("Product ID should not be blank")
            }
        }
    }
    
    data class Create(
        val name: String,
        val price: Long,
        val stock: Long
    ) : ProductCommand() {
        init {
            if (name.isBlank()) {
                throw ProductException.NameShouldNotBlank("Product name should not be blank")
            }
            if (price <= 0) {
                throw ProductException.PriceShouldMoreThan0("Price should be more than 0")
            }
            if (stock < 0) {
                throw ProductException.StockShouldNotNegative("Stock should not be negative")
            }
        }
        
        fun toEntity(): Product {
            return Product(
                productId = UUID.randomUUID().toString(),
                name = name,
                price = price,
                stock = stock,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
    
    data class DecreaseStock(
        val productId: String,
        val amount: Long
    ) : ProductCommand() {
        init {
            if (productId.isBlank()) {
                throw ProductException.ProductIdShouldNotBlank("Product ID should not be blank")
            }
            if (amount <= 0) {
                throw ProductException.AmountShouldMoreThan0("Amount should be more than 0")
            }
        }
    }
}
```

### 4.4 원칙

- 모든 커맨드는 `sealed class` 내부에 정의합니다.
- 각 커맨드는 필요한 모든 데이터를 포함해야 합니다.
- 검증 로직은 커맨드 객체의 `init` 블록에서 수행합니다.
- 도메인 서비스 메서드는 커맨드 객체를 파라미터로 받아 처리합니다.

## 5. 예외 처리

### 5.1 정의

도메인 레이어에서는 비즈니스 규칙 위반이나 도메인 로직 처리 중 발생하는 오류를 표현하기 위해 도메인 예외를 사용합니다.

### 5.2 명명 규칙

- 도메인 예외 클래스는 `{도메인}Exception` 형식으로 명명합니다. (예: `UserException`, `OrderException`)
- 내부 예외 클래스는 발생 원인을 명확히 표현합니다. (예: `NotFound`, `AlreadyExists`, `InvalidState` 등)

### 5.3 구조

```kotlin
sealed class ProductException(message: String) : RuntimeException(message) {
    class NotFound(message: String) : ProductException("product not found: $message")
    class ProductIdShouldNotBlank(message: String) : ProductException("product id should not blank: $message")
    class NameShouldNotBlank(message: String) : ProductException("product name should not blank: $message")
    class PriceShouldMoreThan0(message: String) : ProductException("price should more than 0: $message")
    class StockShouldNotNegative(message: String) : ProductException("stock should not negative: $message")
    class StockAmountUnderflow(message: String) : ProductException("stock amount underflow: $message")
}
```

### 5.4 원칙

- 모든 도메인 예외는 `RuntimeException`을 상속받아 unchecked exception으로 구현합니다.
- 예외 클래스는 도메인 별로 그룹화합니다.
- 예외 메시지는 명확하고 구체적으로 작성합니다.
- 예외는 발생 즉시 처리하지 않고, 상위 레이어로 전파하여 처리합니다.

## 6. 값 객체

### 6.1 정의

값 객체(Value Object)는, 식별자 없이 속성 값으로만 정의되는 객체입니다. 값 객체는 불변하며, 동일한 속성 값을 가지면 동일한 객체로 간주됩니다.

### 6.2 명명 규칙

- 클래스 이름은 표현하는 개념을 명확히 나타내는 명사를 사용합니다. (예: `Address`, `Money`, `EmailAddress`)

### 6.3 구조

```kotlin
data class Money(
    val amount: Long,
    val currency: Currency
) {
    init {
        require(amount >= 0) { "Amount cannot be negative" }
    }
    
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currencies must match" }
        return Money(amount + other.amount, currency)
    }
    
    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Currencies must match" }
        require(amount >= other.amount) { "Cannot result in negative amount" }
        return Money(amount - other.amount, currency)
    }
}
```

### 6.4 원칙

- 값 객체는 항상 불변으로 설계합니다.
- 값 객체의 동등성은 속성 값의 동등성으로 판단합니다.
- 값 객체는 비즈니스 로직을 포함할 수 있습니다.
- 값 객체는 도메인 규칙을 캡슐화하고 표현력을 높이는 데 활용합니다. 