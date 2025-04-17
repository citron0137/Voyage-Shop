# 주문 아이템 순위 조회 성능 개선 방안

## 문제 상황

현재 `OrderItemRankFacade`의 `getRecentTopOrderItemRanks()` 메서드는 다음과 같은 성능 이슈가 있습니다:

- **문제 기능**: `getRecentTopOrderItemRanks()`
- **원인**: 모든 주문을 메모리에 로드한 후 필터링 및 집계 작업 수행
- **이슈**: 주문 데이터가 많을 경우 메모리 사용량 증가 및 처리 시간 지연
- **코드 위치**: `application/orderitemrank/OrderItemRankFacade.kt`

### 현재 구현의 문제점

```kotlin
@Transactional(readOnly = true)
fun getRecentTopOrderItemRanks(criteria: OrderItemRankCriteria.RecentTopRanks = OrderItemRankCriteria.RecentTopRanks()): OrderItemRankResult.List {
    // 현재 시간으로부터 지정된 일수 전 계산
    val daysAgo = LocalDateTime.now().minusDays(criteria.days.toLong())
    
    // 모든 주문 조회 후 지정된 일수 이내 주문만 필터링
    val recentOrders = orderService.getAllOrders()
        .filter { it.createdAt.isAfter(daysAgo) }
        
    // 최근 주문의 아이템만 조회
    val recentOrderItems = recentOrders.flatMap { order ->
        orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId(order.orderId))
    }
    
    // 상품 ID별로 그룹화하여 주문 횟수를 계산하고 상위 M개만 추출
    val topRanks = recentOrderItems
        .groupBy { it.productId }
        .mapValues { it.value.sumOf { item -> item.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(criteria.limit)
        .map { OrderItemRankResult.Rank(productId = it.first, orderCount = it.second) }
        
    return OrderItemRankResult.List(topRanks)
}
```

위 코드는 다음과 같은 문제가 있습니다:
1. 모든 주문을 메모리에 로드 후 필터링
2. 주문 항목 조회를 위해 N+1 쿼리 발생
3. 메모리 내에서 그룹화 및 집계 작업 수행
4. 데이터 증가에 따른 성능 저하 불가피

## 개선 단계

성능 개선을 위한 조치를 단기, 중기, 장기적 관점에서 다음과 같이 단계별로 적용할 수 있습니다:

### 단기적 개선 (1-2주)
- **인덱스 최적화**: `OrderEntity.createdAt`과 `OrderItemEntity.productId` 컬럼에 인덱스 추가
- **조회 조건 개선**: 메모리에서 필터링하기 전에 DB 쿼리 조건으로 날짜 필터링 적용
- **코드 리팩토링**: 메모리 사용량을 줄이기 위한 코드 최적화

### 중기적 개선 (2-4주)
- **데이터베이스 레벨 집계 쿼리 구현**: JPA 리포지토리에 특화된 쿼리 메서드 추가
- **Facade 로직 개선**: 개선된 쿼리를 사용하도록 Facade 메서드 수정
- **간단한 캐싱 도입**: 자주 요청되는 데이터에 대한 인메모리 캐싱 적용

### 장기적 개선 (1-2개월)
- **통계 테이블 설계 및 구현**: 주문 통계를 위한 별도 테이블 구조 설계
- **배치 작업 구현**: 주문 데이터로부터 통계 데이터를 주기적으로 계산하는 배치 프로세스 개발
- **분산 캐싱 시스템 도입**: Redis 등을 활용한 확장 가능한 캐싱 시스템 구축
- **모니터링 시스템 구축**: 성능 지표 수집 및 모니터링 체계 수립

## 해결 방안

### 1. 데이터베이스 레벨에서 집계 쿼리 활용 (중기)

```kotlin
@Repository
interface OrderItemJpaRepository : JpaRepository<OrderItemEntity, String> {
    /**
     * 최근 N일 동안의 상품별 주문 횟수 상위 M개를 조회합니다.
     */
    @Query("""
        SELECT oi.productId as productId, SUM(oi.amount) as orderCount
        FROM OrderItemEntity oi
        JOIN OrderEntity o ON oi.orderId = o.orderId
        WHERE o.createdAt > :startDate
        GROUP BY oi.productId
        ORDER BY orderCount DESC
        LIMIT :limit
    """)
    fun findTopOrderItemsByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("limit") limit: Int
    ): List<OrderItemRankProjection>
    
    /**
     * 최근 N일 동안의 상품별 주문 횟수를 조회합니다 (페이지네이션 적용).
     */
    @Query("""
        SELECT oi.productId as productId, SUM(oi.amount) as orderCount
        FROM OrderItemEntity oi
        JOIN OrderEntity o ON oi.orderId = o.orderId
        WHERE o.createdAt > :startDate
        GROUP BY oi.productId
        ORDER BY orderCount DESC
    """)
    fun findOrderItemsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        Pageable pageable
    ): Page<OrderItemRankProjection>
}

/**
 * 주문 아이템 순위 조회 결과를 담을 프로젝션 인터페이스
 */
interface OrderItemRankProjection {
    fun getProductId(): String
    fun getOrderCount(): Long
}
```

### 2. 개선된 Facade 구현 (중기)

```kotlin
@Transactional(readOnly = true)
fun getRecentTopOrderItemRanks(criteria: OrderItemRankCriteria.RecentTopRanks = OrderItemRankCriteria.RecentTopRanks()): OrderItemRankResult.List {
    // 현재 시간으로부터 지정된 일수 전 계산
    val daysAgo = LocalDateTime.now().minusDays(criteria.days.toLong())
    
    // DB에서 직접 집계 쿼리 실행
    val topRanks = orderItemRepository.findTopOrderItemsByDateRange(daysAgo, criteria.limit)
        .map { OrderItemRankResult.Rank(productId = it.getProductId(), orderCount = it.getOrderCount()) }
        
    return OrderItemRankResult.List(topRanks)
}
```

### 3. 주기적 통계 데이터 생성 배치 작업 구현 (장기)

주문량이 많고 실시간 계산이 부담스러운 경우, 주기적으로 통계 데이터를 계산하여 별도 테이블에 저장하는 방식을 고려할 수 있습니다.

```kotlin
@Entity
@Table(name = "product_rank_statistics")
class ProductRankStatisticsEntity(
    @Id
    @Column(name = "id")
    val id: String,
    
    @Column(name = "product_id")
    val productId: String,
    
    @Column(name = "rank_period_type")
    @Enumerated(EnumType.STRING)
    val periodType: RankPeriodType,
    
    @Column(name = "order_count")
    val orderCount: Long,
    
    @Column(name = "rank")
    val rank: Int,
    
    @Column(name = "calculated_at")
    val calculatedAt: LocalDateTime
)

enum class RankPeriodType {
    DAILY, WEEKLY, MONTHLY
}
```

### 4. 캐싱 적용 (중기~장기)

```kotlin
@Service
@CacheConfig(cacheNames = ["orderItemRank"])
class OrderItemRankService(
    private val orderItemRepository: OrderItemJpaRepository
) {
    /**
     * 최근 N일간의 주문 아이템 중 상위 M개 순위를 조회합니다.
     * 결과는 캐시됩니다.
     */
    @Cacheable(key = "#days + '-' + #limit")
    fun getRecentTopOrderItemRanks(days: Int, limit: Int): List<OrderItemRankProjection> {
        val daysAgo = LocalDateTime.now().minusDays(days.toLong())
        return orderItemRepository.findTopOrderItemsByDateRange(daysAgo, limit)
    }
    
    /**
     * 주문 발생 시 캐시를 무효화합니다.
     */
    @CacheEvict(allEntries = true)
    fun invalidateCache() {
        // 캐시 무효화
    }
}
```

## 성능 개선 효과

1. **메모리 사용량 감소**
   - 모든 주문 데이터를 메모리에 로드하지 않고, 필요한 결과만 DB에서 계산하여 가져옴

2. **쿼리 수 감소**
   - N+1 쿼리 문제 해결
   - 단일 쿼리로 집계 결과 획득

3. **응답 시간 개선**
   - 데이터베이스 최적화(인덱스 등)를 통한 쿼리 성능 향상
   - 캐싱을 통한 반복 요청 처리 속도 향상

4. **확장성 개선**
   - 데이터 증가에도 일정한 성능 유지
   - 페이지네이션 지원으로 대용량 데이터 처리 가능

## 구현 시 고려사항

1. **인덱스 최적화** (단기)
   - `OrderEntity.createdAt` 컬럼에 인덱스 추가
   - `OrderItemEntity.productId` 컬럼에 인덱스 추가

2. **캐시 만료 정책** (중기)
   - 주문 데이터 변경 시 캐시 무효화
   - 적절한 TTL(Time-To-Live) 설정

3. **통계 테이블 사용 시 일관성** (장기)
   - 주문 데이터 변경과 통계 테이블 업데이트 간의 일관성 확보
   - 배치 작업 주기 최적화

4. **모니터링** (중기~장기)
   - 쿼리 성능 모니터링
   - 캐시 히트율 모니터링
   - 주문량 증가에 따른 성능 변화 추적 