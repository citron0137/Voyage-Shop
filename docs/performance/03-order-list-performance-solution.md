# 주문 목록 조회 성능 개선 방안

## 문제 상황

현재 `OrderFacade`의 `getAllOrders()` 및 `getOrdersByUserId()` 메서드는 다음과 같은 성능 이슈가 있습니다:

- **문제 기능**: `getAllOrders()`, `getOrdersByUserId()`
- **원인**: 모든 주문을 조회한 후 각 주문의 항목과 할인 정보를 추가로 조회
- **이슈**: 주문 데이터가 많을 경우 N+1 쿼리 발생 가능성
- **코드 위치**: `application/order/OrderFacade.kt`

### 현재 구현의 문제점

```kotlin
@Transactional(readOnly = true)
fun getAllOrders(criteria: OrderCriteria.GetAll): OrderResult.Orders {
    val orders = orderService.getAllOrders()
    
    // 주문이 없으면 빈 목록 반환
    if (orders.isEmpty()) {
        return OrderResult.Orders(emptyList())
    }
    
    // 각 주문의 항목과 할인 정보 조회
    val orderIds = orders.map { it.orderId }
    val allItems = orderIds.flatMap { 
        orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId(it)) 
    }
    val allDiscounts = orderIds.flatMap { 
        orderService.getOrderDiscountsByOrderId(OrderDiscountCommand.GetByOrderId(it)) 
    }
    
    // 주문 ID별로 그룹화
    val itemsByOrderId = allItems.groupBy { it.orderId }
    val discountsByOrderId = allDiscounts.groupBy { it.orderId }
    
    return OrderResult.Orders.fromWithDetails(orders, itemsByOrderId, discountsByOrderId)
}
```

위 코드는 다음과 같은 문제가 있습니다:

1. 모든 주문을 한 번에 메모리에 로드
2. 주문 목록에 있는 각 주문 ID에 대해 항목과 할인 정보를 별도로 조회 (N+1 쿼리 문제)
3. 데이터가 많을 경우 메모리 사용량 급증
4. 페이지네이션이 적용되지 않아 대량 데이터 처리 시 성능 저하

## 개선 단계

성능 개선을 위한 조치를 단기, 중기, 장기적 관점에서 다음과 같이 단계별로 적용할 수 있습니다:

### 단기적 개선 (1-2주)
- **인덱스 최적화**: `OrderEntity.userId`, `OrderEntity.createdAt` 등 주요 조회 컬럼에 인덱스 추가
- **코드 리팩토링**: 불필요한 정보 로딩 최소화 및 조건부 로딩 적용
- **기본 페이지네이션 적용**: 기존 API에 제한적 페이지네이션 도입 (예: 최대 100건 제한)

### 중기적 개선 (2-4주)
- **API 페이지네이션 지원**: 클라이언트가 페이지 번호와 크기를 지정할 수 있는 API 개선
- **JOIN FETCH 쿼리 구현**: 연관 데이터를 한 번에 조회하는 JPA 리포지토리 쿼리 추가
- **지연 로딩과 배치 크기 설정**: 엔티티 관계 정의에 배치 사이즈 최적화 적용
- **기본 캐싱 적용**: 자주 조회되는 주문 정보에 대한 인메모리 캐싱 적용

### 장기적 개선 (1-2개월)
- **DTO 프로젝션 전면 도입**: 모든 조회 API에 최적화된 DTO 프로젝션 적용
- **분산 캐싱 시스템 구축**: Redis 등을 활용한 확장 가능한 캐싱 레이어 구축
- **읽기 전용 복제본 활용**: 조회 쿼리를 위한 데이터베이스 읽기 전용 복제본 구성
- **모니터링 시스템 구축**: 성능 지표 수집 및 데이터 증가에 따른 모니터링 체계 수립

## 해결 방안

### 1. 페이지네이션 도입 (단기)

```kotlin
@Repository
interface OrderJpaRepository : JpaRepository<OrderEntity, String> {
    /**
     * 사용자 ID로 주문을 페이지네이션하여 조회합니다.
     */
    fun findByUserId(userId: String, pageable: Pageable): Page<OrderEntity>
    
    /**
     * 모든 주문을 페이지네이션하여 조회합니다.
     */
    fun findAll(pageable: Pageable): Page<OrderEntity>
}
```

### 2. JOIN과 함께 즉시 로딩(Eager Loading) 구현 (중기)

```kotlin
@Repository
interface OrderJpaRepository : JpaRepository<OrderEntity, String> {
    /**
     * 주문을 항목 및 할인 정보와 함께 조회합니다.
     */
    @Query("""
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN FETCH o.orderItems
        LEFT JOIN FETCH o.orderDiscounts
        WHERE o.orderId IN :orderIds
    """)
    fun findByOrderIdInWithItemsAndDiscounts(@Param("orderIds") orderIds: List<String>): List<OrderEntity>
    
    /**
     * 사용자 ID로 주문을 조회하고 주문 ID 목록을 반환합니다.
     */
    @Query("SELECT o.orderId FROM OrderEntity o WHERE o.userId = :userId")
    fun findOrderIdsByUserId(@Param("userId") userId: String, pageable: Pageable): Page<String>
    
    /**
     * 모든 주문의 ID를 페이지네이션하여 조회합니다.
     */
    @Query("SELECT o.orderId FROM OrderEntity o")
    fun findAllOrderIds(pageable: Pageable): Page<String>
}
```

### 3. 개선된 Facade 구현 (페이지네이션 적용) (중기)

```kotlin
@Transactional(readOnly = true)
fun getOrdersByUserId(criteria: OrderCriteria.GetByUserId, pageable: Pageable): Page<OrderResult.Get> {
    // 사용자 존재 여부 확인
    userService.findUserByIdOrThrow(criteria.userId)
    
    // 1단계: 주문 ID만 페이지네이션하여 조회
    val orderIdsPage = orderJpaRepository.findOrderIdsByUserId(criteria.userId, pageable)
    
    if (orderIdsPage.isEmpty) {
        return Page.empty(pageable)
    }
    
    // 2단계: 조회된 주문 ID에 대해 항목과 할인 정보를 함께 조회
    val orderIds = orderIdsPage.content
    val ordersWithDetails = orderJpaRepository.findByOrderIdInWithItemsAndDiscounts(orderIds)
    
    // 결과 변환
    val orderResults = ordersWithDetails.map { entity ->
        val order = OrderEntity.toDomain(entity)
        val items = entity.orderItems.map { OrderItemEntity.toDomain(it) }
        val discounts = entity.orderDiscounts.map { OrderDiscountEntity.toDomain(it) }
        OrderResult.Get.from(order, items, discounts)
    }
    
    return PageImpl(orderResults, pageable, orderIdsPage.totalElements)
}
```

### 4. 쿼리 최적화를 위한 DTO 프로젝션 활용 (장기)

```kotlin
interface OrderSummaryDto {
    val orderId: String
    val userId: String
    val totalAmount: Long
    val finalAmount: Long
    val createdAt: LocalDateTime
}

@Repository
interface OrderJpaRepository : JpaRepository<OrderEntity, String> {
    /**
     * 주문 요약 정보만 페이지네이션하여 조회합니다.
     */
    @Query("""
        SELECT 
            o.orderId as orderId,
            o.userId as userId,
            o.totalAmount as totalAmount,
            o.finalAmount as finalAmount,
            o.createdAt as createdAt
        FROM OrderEntity o
        WHERE o.userId = :userId
        ORDER BY o.createdAt DESC
    """)
    fun findOrderSummariesByUserId(
        @Param("userId") userId: String, 
        pageable: Pageable
    ): Page<OrderSummaryDto>
}
```

### 5. 지연 로딩과 배치 크기 설정 (중기)

엔티티 관계 설정 시 배치 크기를 지정하여 N+1 문제를 완화할 수 있습니다.

```kotlin
@Entity
@Table(name = "orders")
class OrderEntity(
    // 다른 필드들...
    
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "orderId", fetch = FetchType.LAZY)
    val orderItems: List<OrderItemEntity> = emptyList(),
    
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "orderId", fetch = FetchType.LAZY)
    val orderDiscounts: List<OrderDiscountEntity> = emptyList()
)
```

### 6. 캐싱 적용 (중기~장기)

```kotlin
@Service
@CacheConfig(cacheNames = ["orderCache"])
class CachedOrderService(
    private val orderJpaRepository: OrderJpaRepository
) {
    /**
     * 사용자 ID로 주문을 조회합니다.
     * 결과는 캐시됩니다.
     */
    @Cacheable(key = "'user_' + #userId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    fun getOrdersByUserId(userId: String, pageable: Pageable): Page<OrderEntity> {
        return orderJpaRepository.findByUserId(userId, pageable)
    }
    
    /**
     * 주문 ID로 주문 상세 정보를 조회합니다.
     * 결과는 캐시됩니다.
     */
    @Cacheable(key = "'order_' + #orderId")
    fun getOrderWithDetails(orderId: String): OrderEntity? {
        val result = orderJpaRepository.findByOrderIdInWithItemsAndDiscounts(listOf(orderId))
        return result.firstOrNull()
    }
    
    /**
     * 주문 생성 또는 수정 시 캐시를 무효화합니다.
     */
    @CacheEvict(allEntries = true)
    fun invalidateCache() {
        // 캐시 무효화
    }
}
```

## 성능 개선 효과

1. **쿼리 수 감소**
   - JOIN FETCH 및 배치 로딩으로 N+1 쿼리 문제 해결
   - 필요한 데이터만 조회하는 DTO 프로젝션으로 쿼리 최적화

2. **메모리 사용량 감소**
   - 페이지네이션을 통해 한 번에 처리하는 데이터 양 제한
   - 필요한 필드만 조회하는 프로젝션으로 데이터 전송량 최소화

3. **응답 시간 개선**
   - 캐싱을 통한 반복 요청 처리 속도 향상
   - 최적화된 쿼리로 데이터베이스 부하 감소

4. **확장성 개선**
   - 페이지네이션 지원으로 대용량 데이터 처리 가능
   - 데이터 증가에도 일정한 성능 유지

## 구현 시 고려사항

1. **인덱스 최적화** (단기)
   - `OrderEntity.userId` 컬럼에 인덱스 추가
   - `OrderEntity.createdAt` 컬럼에 인덱스 추가
   - 필요한 경우 복합 인덱스 고려

2. **적절한 페이지 크기 선택** (단기)
   - 일반적으로 10-50개 항목이 적절
   - 실제 사용 패턴에 따라 조정

3. **캐시 정책 최적화** (중기)
   - 읽기가 많고 쓰기가 적은 데이터에 캐싱 우선 적용
   - 적절한 캐시 만료 시간 설정

4. **API 설계 변경** (중기)
   - 클라이언트에 페이지네이션 파라미터 제공 (페이지 번호, 크기)
   - 응답에 페이지 메타데이터 포함 (전체 항목 수, 전체 페이지 수 등)

5. **모니터링** (장기)
   - 쿼리 실행 계획 및 실행 시간 모니터링
   - 캐시 히트율 모니터링
   - 주문 데이터 증가에 따른 성능 변화 추적 