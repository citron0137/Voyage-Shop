# 락(Lock)을 사용하는 조회 기능 성능 개선 방안

## 문제 상황

현재 `UserPointJpaRepository`와 `ProductJpaRepository` 등에서 사용하는 락(Lock) 관련 기능은 다음과 같은 성능 이슈가 있습니다:

- **문제 기능**: `findByUserIdWithLock()`, `findByIdWithLock()` 등
- **원인**: Pessimistic Lock 사용으로 인한 트랜잭션 경합
- **이슈**: 동시 접속자가 많을 경우 대기 시간 증가
- **코드 위치**: 
  - `infrastructure/userpoint/UserPointJpaRepository.kt`
  - `infrastructure/product/ProductJpaRepository.kt`

### 현재 구현의 문제점

```kotlin
// UserPointJpaRepository.kt
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM UserPointEntity p WHERE p.userId = :userId")
fun findByUserIdWithLock(userId: String): UserPointEntity?

// ProductJpaRepository.kt
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM ProductEntity p WHERE p.productId = :id")
fun findByIdWithLock(id: String): ProductEntity?
```

위 코드는 다음과 같은 문제가 있습니다:

1. 비관적 락(Pessimistic Lock)은 트랜잭션이 완료될 때까지 해당 레코드에 대한 락을 보유
2. 동시 접속자가 많을 경우 락 획득 대기 시간이 길어지고 데이터베이스 성능 저하
3. 읽기 작업에도 동일하게 락이 적용되어 불필요한 경합 발생
4. 트랜잭션 타임아웃 및 데드락 발생 가능성

## 개선 단계

성능 개선을 위한 조치를 단기, 중기, 장기적 관점에서 다음과 같이 단계별로 적용할 수 있습니다:

### 단기적 개선 (1-2주)
- **락 범위 최소화**: 실제 갱신이 필요한 부분에만 락 적용
- **트랜잭션 시간 최소화**: 락을 획득한 트랜잭션의 실행 시간 단축
- **타임아웃 설정 최적화**: 락 획득 대기 시간 제한 설정
- **락 획득 재시도 로직 추가**: 락 획득 실패 시 재시도 메커니즘 구현

### 중기적 개선 (2-4주)
- **낙관적 락(Optimistic Lock) 도입**: 일부 기능에 비관적 락 대신 낙관적 락 적용
- **읽기/쓰기 분리**: 읽기 전용 작업은 락 없이 처리하도록 API 분리
- **분산 락 매니저 도입**: 애플리케이션 레벨의 락 관리 시스템 구현

### 장기적 개선 (1-2개월)
- **이벤트 기반 아키텍처 도입**: 동시성 제어가 필요한 작업을 비동기 이벤트로 처리
- **CQRS 패턴 적용**: Command와 Query 책임 분리를 통한 락 사용 최소화
- **샤딩 전략 도입**: 사용자 또는 상품 데이터 샤딩으로 락 경합 감소

## 해결 방안

### 1. 락 범위 최소화 및 트랜잭션 최적화 (단기)

```kotlin
// 기존 구현
@Transactional
fun decreaseStock(productId: String, amount: Long): Product {
    val product = productRepository.findByIdWithLock(productId)
        ?: throw ProductException.NotFound("Product with id: $productId")
    
    // 재고 체크 및 감소 로직...
    // 여러 작업 수행...
    
    return productRepository.update(product)
}

// 개선된 구현
@Transactional
fun decreaseStock(productId: String, amount: Long): Product {
    // 락 획득 전에 필요한 검증 작업 수행
    val product = productRepository.findById(productId)
        ?: throw ProductException.NotFound("Product with id: $productId")
    
    if (product.stock < amount) {
        throw ProductException.StockAmountUnderflow("Insufficient stock")
    }
    
    // 실제 업데이트 작업에만 락 사용
    return executeWithLock(productId) {
        val lockedProduct = productRepository.findByIdWithLock(productId)
            ?: throw ProductException.NotFound("Product with id: $productId")
        
        // 최소한의 작업만 수행
        lockedProduct.decreaseStock(amount)
        productRepository.update(lockedProduct)
    }
}

// 락 획득 및 재시도 로직
private fun <T> executeWithLock(resourceId: String, maxRetries: Int = 3, action: () -> T): T {
    var retryCount = 0
    var lastException: Exception? = null
    
    while (retryCount < maxRetries) {
        try {
            return action()
        } catch (e: PessimisticLockingFailureException) {
            lastException = e
            retryCount++
            // 지수 백오프 적용
            Thread.sleep((100 * (2.0.pow(retryCount.toDouble()))).toLong())
        }
    }
    
    throw lastException ?: RuntimeException("Failed to acquire lock after $maxRetries retries")
}
```

### 2. 낙관적 락(Optimistic Lock) 도입 (중기)

```kotlin
// 엔티티에 버전 필드 추가
@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @Column(name = "product_id")
    val productId: String,
    
    @Column(name = "name")
    val name: String,
    
    @Column(name = "price")
    val price: Long,
    
    @Column(name = "stock")
    var stock: Long,
    
    @Version
    @Column(name = "version")
    var version: Long = 0,
    
    // 다른 필드들...
)

// 낙관적 락 활용 서비스
@Service
class OptimisticProductService(
    private val productRepository: ProductRepository
) {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun decreaseStock(productId: String, amount: Long): Product {
        var retry = 0
        val maxRetries = 5
        
        while (retry < maxRetries) {
            try {
                val product = productRepository.findById(productId)
                    ?: throw ProductException.NotFound("Product with id: $productId")
                
                if (product.stock < amount) {
                    throw ProductException.StockAmountUnderflow("Insufficient stock")
                }
                
                product.stock -= amount
                return productRepository.update(product)
            } catch (ex: ObjectOptimisticLockingFailureException) {
                // 낙관적 락 실패 시 재시도
                retry++
                if (retry >= maxRetries) {
                    throw ConcurrencyException("Failed to update product after $maxRetries attempts")
                }
                
                // 재시도 전 짧은 대기
                Thread.sleep(50)
            }
        }
        
        throw RuntimeException("Unexpected error in decreaseStock")
    }
}
```

### 3. 읽기/쓰기 분리 (중기)

```kotlin
@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, String> {
    // 읽기 전용 쿼리 - 락 없음
    @Query("SELECT p FROM ProductEntity p WHERE p.productId = :id")
    fun findByIdForRead(id: String): ProductEntity?
    
    // 쓰기용 쿼리 - 락 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.productId = :id")
    fun findByIdForWrite(id: String): ProductEntity?
}

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    // 읽기 전용 트랜잭션
    @Transactional(readOnly = true)
    fun getProduct(id: String): Product {
        return productRepository.findByIdForRead(id)
            ?: throw ProductException.NotFound("Product with id: $id")
    }
    
    // 쓰기 트랜잭션
    @Transactional
    fun updateProduct(id: String, updateFunc: (Product) -> Product): Product {
        val product = productRepository.findByIdForWrite(id)
            ?: throw ProductException.NotFound("Product with id: $id")
        
        val updatedProduct = updateFunc(product)
        return productRepository.update(updatedProduct)
    }
}
```

### 4. 분산 락 매니저 도입 (중기)

```kotlin
@Service
class RedisLockManager(
    private val redisTemplate: StringRedisTemplate
) {
    private val lockTimeout = 30L // 30초 타임아웃
    
    fun acquireLock(resourceId: String, waitTime: Long, leaseTime: Long): Boolean {
        val lockKey = "lock:$resourceId"
        val lockValue = UUID.randomUUID().toString()
        val deadline = System.currentTimeMillis() + waitTime
        
        while (System.currentTimeMillis() < deadline) {
            // Redis를 사용한 분산 락 획득 시도
            val acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofMillis(leaseTime))
            
            if (acquired == true) {
                return true
            }
            
            // 짧은 대기 후 재시도
            Thread.sleep(100)
        }
        
        return false
    }
    
    fun releaseLock(resourceId: String) {
        val lockKey = "lock:$resourceId"
        redisTemplate.delete(lockKey)
    }
}

// 분산 락 매니저 활용 서비스
@Service
class DistributedLockProductService(
    private val productRepository: ProductRepository,
    private val lockManager: RedisLockManager
) {
    @Transactional
    fun decreaseStock(productId: String, amount: Long): Product {
        // 분산 락 획득 시도
        if (!lockManager.acquireLock(productId, 5000, 10000)) {
            throw ConcurrencyException("Failed to acquire lock for product: $productId")
        }
        
        try {
            val product = productRepository.findById(productId)
                ?: throw ProductException.NotFound("Product with id: $productId")
            
            if (product.stock < amount) {
                throw ProductException.StockAmountUnderflow("Insufficient stock")
            }
            
            product.stock -= amount
            return productRepository.update(product)
        } finally {
            // 락 해제
            lockManager.releaseLock(productId)
        }
    }
}
```

### 5. 이벤트 기반 아키텍처 도입 (장기)

```kotlin
// 이벤트 클래스
data class StockDecreaseEvent(
    val productId: String,
    val amount: Long,
    val orderId: String
)

// 이벤트 발행자
@Service
class ProductEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, StockDecreaseEvent>
) {
    fun publishStockDecreaseEvent(productId: String, amount: Long, orderId: String) {
        val event = StockDecreaseEvent(productId, amount, orderId)
        kafkaTemplate.send("stock-events", productId, event)
    }
}

// 이벤트 리스너
@Service
class StockEventListener(
    private val productRepository: ProductRepository
) {
    @KafkaListener(topics = ["stock-events"], groupId = "stock-processor")
    fun handleStockDecreaseEvent(event: StockDecreaseEvent) {
        // 낙관적 락과 재시도 로직 포함
        var retryCount = 0
        val maxRetries = 5
        
        while (retryCount < maxRetries) {
            try {
                val product = productRepository.findById(event.productId)
                    ?: throw ProductException.NotFound("Product not found: ${event.productId}")
                
                if (product.stock < event.amount) {
                    // 재고 부족 시 보상 트랜잭션 처리
                    handleInsufficientStock(event)
                    return
                }
                
                product.stock -= event.amount
                productRepository.update(product)
                return
            } catch (ex: ObjectOptimisticLockingFailureException) {
                retryCount++
                if (retryCount >= maxRetries) {
                    // 최대 재시도 초과 시 실패 처리
                    handleStockUpdateFailure(event)
                    return
                }
                
                // 지수 백오프 적용
                Thread.sleep((100 * (2.0.pow(retryCount.toDouble()))).toLong())
            }
        }
    }
    
    private fun handleInsufficientStock(event: StockDecreaseEvent) {
        // 재고 부족 시 보상 트랜잭션 처리 로직
    }
    
    private fun handleStockUpdateFailure(event: StockDecreaseEvent) {
        // 업데이트 실패 시 처리 로직
    }
}

// 서비스 클래스
@Service
class EventDrivenProductService(
    private val productRepository: ProductRepository,
    private val eventPublisher: ProductEventPublisher
) {
    // 비동기 재고 감소 요청
    @Transactional(readOnly = true)
    fun requestStockDecrease(productId: String, amount: Long, orderId: String): Boolean {
        // 재고 확인만 수행 (락 없음)
        val product = productRepository.findById(productId)
            ?: throw ProductException.NotFound("Product with id: $productId")
        
        // 현재 재고 확인 (동시성 제어 없이 단순 체크)
        if (product.stock < amount) {
            return false
        }
        
        // 이벤트 발행
        eventPublisher.publishStockDecreaseEvent(productId, amount, orderId)
        return true
    }
}
```

### 6. CQRS 패턴 적용 (장기)

```kotlin
// 쿼리 모델 (읽기 전용)
@Entity
@Table(name = "product_read_models")
class ProductReadModel(
    @Id
    @Column(name = "product_id")
    val productId: String,
    
    @Column(name = "name")
    val name: String,
    
    @Column(name = "price")
    val price: Long,
    
    @Column(name = "stock")
    val stock: Long,
    
    // 다른 읽기 전용 필드들...
)

// 명령 모델 (쓰기 전용)
@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @Column(name = "product_id")
    val productId: String,
    
    @Column(name = "stock")
    var stock: Long,
    
    @Version
    @Column(name = "version")
    var version: Long = 0
    
    // 다른 필수 필드들...
)

// 쿼리 리포지토리 (읽기 전용)
@Repository
interface ProductQueryRepository : JpaRepository<ProductReadModel, String> {
    // 읽기 전용 쿼리만 포함
}

// 명령 리포지토리 (쓰기 전용)
@Repository
interface ProductCommandRepository : JpaRepository<ProductEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.productId = :id")
    fun findByIdWithLock(id: String): ProductEntity?
}

// 쿼리 서비스 (읽기 전용)
@Service
@Transactional(readOnly = true)
class ProductQueryService(
    private val productQueryRepository: ProductQueryRepository
) {
    fun getProduct(id: String): ProductReadModel {
        return productQueryRepository.findById(id).orElseThrow {
            ProductException.NotFound("Product with id: $id")
        }
    }
    
    fun getAllProducts(pageable: Pageable): Page<ProductReadModel> {
        return productQueryRepository.findAll(pageable)
    }
}

// 명령 서비스 (쓰기 전용)
@Service
class ProductCommandService(
    private val productCommandRepository: ProductCommandRepository,
    private val eventPublisher: EventPublisher
) {
    @Transactional
    fun decreaseStock(command: DecreaseStockCommand): ProductEntity {
        val product = productCommandRepository.findByIdWithLock(command.productId)
            ?: throw ProductException.NotFound("Product with id: ${command.productId}")
        
        if (product.stock < command.amount) {
            throw ProductException.StockAmountUnderflow("Insufficient stock")
        }
        
        product.stock -= command.amount
        val updatedProduct = productCommandRepository.save(product)
        
        // 읽기 모델 업데이트를 위한 이벤트 발행
        eventPublisher.publish(ProductStockUpdatedEvent(command.productId, updatedProduct.stock))
        
        return updatedProduct
    }
}
```

## 성능 개선 효과

1. **처리량 증가**
   - 락 경합 감소로 인한 병렬 처리 능력 향상
   - 동시 접속자가 많아도 안정적인 성능 유지

2. **응답 시간 개선**
   - 락 대기 시간 감소
   - 읽기 작업에 대한 불필요한 락 제거

3. **시스템 안정성 향상**
   - 데드락 발생 가능성 감소
   - 타임아웃 및 실패 처리 메커니즘 개선

4. **확장성 개선**
   - 트래픽 증가에도 부드러운 성능 확장
   - 분산 시스템에서의 효율적인 동시성 제어

## 구현 시 고려사항

1. **트랜잭션 격리 수준 최적화** (단기)
   - READ_COMMITTED 격리 수준 사용으로 불필요한 락 감소
   - 트랜잭션 범위 최소화

2. **DB 인덱스 최적화** (단기)
   - 락을 사용하는 쿼리에 사용되는 컬럼에 적절한 인덱스 설정
   - 락 획득 속도 개선을 위한 인덱스 전략

3. **모니터링 체계 구축** (중기)
   - 락 대기 시간 및 경합 상황 모니터링
   - 데드락 및 타임아웃 발생 추적

4. **테스트 전략** (중기)
   - 동시성 테스트 시나리오 개발
   - 부하 테스트를 통한 락 전략 검증

5. **장애 복구 전략** (장기)
   - 락 관련 장애 상황에 대한 복구 방안 마련
   - 분산 시스템에서의 데이터 일관성 유지 전략 