# 쿠폰 사용자 정보 및 상품 조회 성능 개선 방안

본 문서는 다음 두 가지 성능 이슈에 대한 해결 방안을 단기, 중기, 장기적 관점에서 정리한 것입니다:
- 쿠폰 사용자 정보 조회 (CouponUserService)
- 상품 전체 조회 (ProductFacade)

## Part 1: 쿠폰 사용자 정보 조회 성능 개선

### 문제 상황

현재 `CouponUserService`의 `getAllCouponUsers()` 및 `getAllCouponsByUserId()` 메서드는 다음과 같은 성능 이슈가 있습니다:

- **문제 기능**: `getAllCouponUsers()`, `getAllCouponsByUserId()`
- **원인**: 사용자의 모든 쿠폰 정보를 한 번에 조회
- **이슈**: 쿠폰 데이터가 많을 경우 조회 시간 지연
- **코드 위치**: `domain/coupon/CouponUserService.kt`

### 현재 구현의 문제점

```kotlin
fun getAllCouponsByUserId(userId: String, userService: UserService): List<CouponUser> {
    if (userId.isBlank()) {
        throw UserException.UserIdShouldNotBlank("유저 ID는 비어있을 수 없습니다.")
    }
    
    // 유저 존재 여부 확인
    userService.findUserByIdOrThrow(userId)
    
    return repository.findByUserId(userId)
}

fun getAllCouponUsers(): List<CouponUser> {
    return repository.findAll()
}
```

위 코드는 다음과 같은 문제가 있습니다:
1. 모든 쿠폰 데이터를 한 번에 메모리에 로드
2. 페이지네이션이 적용되지 않아 대량 데이터 처리 시 성능 저하
3. 불필요한 쿠폰 상태(사용됨, 만료됨 등)까지 모두 조회
4. 데이터 증가에 따른 성능 저하 불가피

### 개선 단계

성능 개선을 위한 조치를 단기, 중기, 장기적 관점에서 다음과 같이 단계별로 적용할 수 있습니다:

#### 단기적 개선 (1-2주)
- **인덱스 최적화**: `CouponUserEntity.userId` 컬럼에 인덱스 추가
- **기본 페이지네이션 도입**: 기본 API에 제한적 페이지네이션 추가 (예: 최대 100건 제한)
- **쿠폰 상태 필터링**: 사용 가능한 쿠폰만 우선 조회하는 필터 옵션 추가

#### 중기적 개선 (2-4주)
- **API 페이지네이션 지원**: 클라이언트가 페이지 번호와 크기를 지정할 수 있는 API 개선
- **조건부 조회 기능 강화**: 만료 일자, 쿠폰 타입 등 다양한 필터링 조건 지원
- **DTO 프로젝션 적용**: 필요한 데이터만 선택적으로 조회하는 최적화

#### 장기적 개선 (1-2개월)
- **캐싱 시스템 구축**: 자주 조회되는 사용자 쿠폰 정보에 대한 캐싱 적용
- **사용자별 쿠폰 요약 정보 저장**: 별도 테이블에 사용자별 쿠폰 요약 정보 관리
- **쿠폰 만료 배치 처리**: 만료된 쿠폰을 별도 처리하여 활성 쿠폰 테이블 크기 최적화

### 해결 방안

#### 1. 페이지네이션 도입 (단기)

```kotlin
@Repository
interface CouponUserJpaRepository : JpaRepository<CouponUserEntity, String> {
    /**
     * 사용자 ID로 쿠폰을 페이지네이션하여 조회합니다.
     */
    fun findByUserId(userId: String, pageable: Pageable): Page<CouponUserEntity>
    
    /**
     * 모든 쿠폰을 페이지네이션하여 조회합니다.
     */
    fun findAll(pageable: Pageable): Page<CouponUserEntity>
}
```

#### 2. 조건부 조회 및 필터링 개선 (중기)

```kotlin
@Repository
interface CouponUserJpaRepository : JpaRepository<CouponUserEntity, String> {
    /**
     * 사용자 ID와 쿠폰 상태로 쿠폰을 조회합니다.
     */
    @Query("""
        SELECT c FROM CouponUserEntity c
        WHERE c.userId = :userId
        AND c.status = :status
        AND c.expiryDate > :now
        ORDER BY c.expiryDate ASC
    """)
    fun findByUserIdAndStatusAndNotExpired(
        @Param("userId") userId: String,
        @Param("status") status: CouponStatus,
        @Param("now") now: LocalDateTime,
        pageable: Pageable
    ): Page<CouponUserEntity>
    
    /**
     * 사용자 ID별 사용 가능한 쿠폰 수를 조회합니다.
     */
    @Query("""
        SELECT COUNT(c)
        FROM CouponUserEntity c
        WHERE c.userId = :userId
        AND c.status = 'ACTIVE'
        AND c.expiryDate > :now
    """)
    fun countAvailableCoupons(
        @Param("userId") userId: String,
        @Param("now") now: LocalDateTime
    ): Long
}
```

#### 3. 개선된 Service 구현 (중기)

```kotlin
@Transactional(readOnly = true)
fun getAvailableCouponsByUserId(
    userId: String,
    userService: UserService,
    pageable: Pageable
): Page<CouponUser> {
    // 유저 존재 여부 확인
    userService.findUserByIdOrThrow(userId)
    
    val now = LocalDateTime.now()
    return couponUserJpaRepository.findByUserIdAndStatusAndNotExpired(
        userId, 
        CouponStatus.ACTIVE, 
        now, 
        pageable
    ).map { CouponUserEntity.toDomain(it) }
}
```

#### 4. 캐싱 적용 (장기)

```kotlin
@Service
@CacheConfig(cacheNames = ["couponCache"])
class CachedCouponUserService(
    private val couponUserRepository: CouponUserRepository
) {
    /**
     * 사용자의 사용 가능한 쿠폰 수를 조회합니다.
     * 결과는 캐시됩니다.
     */
    @Cacheable(key = "'user_available_count_' + #userId")
    fun getAvailableCouponCount(userId: String): Long {
        val now = LocalDateTime.now()
        return couponUserRepository.countAvailableCoupons(userId, now)
    }
    
    /**
     * 사용자 ID로 쿠폰을 조회합니다.
     * 결과는 캐시됩니다.
     */
    @Cacheable(key = "'user_coupons_' + #userId + '_page_' + #pageable.pageNumber")
    fun getAvailableCouponsByUserId(userId: String, pageable: Pageable): Page<CouponUser> {
        val now = LocalDateTime.now()
        return couponUserRepository.findByUserIdAndStatusAndNotExpired(
            userId, CouponStatus.ACTIVE, now, pageable
        ).map { CouponUserEntity.toDomain(it) }
    }
    
    /**
     * 쿠폰 발급 또는 사용 시 캐시를 무효화합니다.
     */
    @CacheEvict(key = "'user_coupons_' + #userId + '*'")
    fun invalidateUserCouponCache(userId: String) {
        // 캐시 무효화
    }
}
```

#### 5. 쿠폰 요약 정보 테이블 도입 (장기)

```kotlin
@Entity
@Table(name = "coupon_user_summary")
class CouponUserSummaryEntity(
    @Id
    @Column(name = "user_id")
    val userId: String,
    
    @Column(name = "active_coupon_count")
    val activeCouponCount: Int,
    
    @Column(name = "used_coupon_count")
    val usedCouponCount: Int,
    
    @Column(name = "expired_coupon_count")
    val expiredCouponCount: Int,
    
    @Column(name = "last_updated_at")
    val lastUpdatedAt: LocalDateTime
)
```

## Part 2: 상품 전체 조회 성능 개선

### 문제 상황

현재 `ProductFacade`의 `getAllProducts()` 메서드는 다음과 같은 성능 이슈가 있습니다:

- **문제 기능**: `getAllProducts()`
- **원인**: 페이지네이션 없이 모든 상품 정보 조회
- **이슈**: 상품 데이터가 많을 경우 응답 시간 지연
- **코드 위치**: `application/product/ProductFacade.kt`

### 현재 구현의 문제점

```kotlin
@Transactional(readOnly = true)
fun getAllProducts(criteria: ProductCriteria.GetAll = ProductCriteria.GetAll()): ProductResult.List {
    val products = productService.getAllProducts()
    return ProductResult.List.from(products)
}
```

위 코드는 다음과 같은 문제가 있습니다:
1. 모든 상품 정보를 한 번에 메모리에 로드
2. 페이지네이션이 적용되지 않아 대량 데이터 처리 시 성능 저하
3. 필요 이상의 데이터(전체 상품 정보)를 조회
4. 데이터 증가에 따른 성능 저하 불가피

### 개선 단계

성능 개선을 위한 조치를 단기, 중기, 장기적 관점에서 다음과 같이 단계별로 적용할 수 있습니다:

#### 단기적 개선 (1-2주)
- **인덱스 최적화**: 검색, 정렬에 자주 사용되는 필드에 인덱스 추가
- **기본 페이지네이션 적용**: 한 번에 조회되는 상품 수 제한 (예: 기본 20개)
- **반환 필드 최소화**: 필요한 정보만 반환하도록 DTO 최적화

#### 중기적 개선 (2-4주)
- **검색 및 필터링 최적화**: 다양한 검색 조건과 필터링 지원
- **API 페이지네이션 고도화**: 커서 기반 페이지네이션 도입
- **정렬 옵션 최적화**: 인덱스를 활용한 효율적인 정렬 구현

#### 장기적 개선 (1-2개월)
- **캐시 시스템 구축**: 자주 조회되는 상품 목록에 대한 캐싱 적용
- **검색 엔진 도입**: Elasticsearch 등을 활용한 고성능 상품 검색 기능 구현
- **상품 데이터 샤딩**: 대량 상품 데이터 처리를 위한 데이터베이스 샤딩 전략 수립

### 해결 방안

#### 1. 페이지네이션 도입 (단기)

```kotlin
@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, String> {
    /**
     * 모든 상품을 페이지네이션하여 조회합니다.
     */
    fun findAll(pageable: Pageable): Page<ProductEntity>
    
    /**
     * 상품 카테고리별로 상품을 페이지네이션하여 조회합니다.
     */
    fun findByCategory(category: String, pageable: Pageable): Page<ProductEntity>
}
```

#### 2. 검색 및 필터링 최적화 (중기)

```kotlin
@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, String> {
    /**
     * 다양한 조건으로 상품을 검색합니다.
     */
    @Query("""
        SELECT p FROM ProductEntity p
        WHERE (:name IS NULL OR p.name LIKE %:name%)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:category IS NULL OR p.category = :category)
        AND (:inStock IS NULL OR (p.stock > 0) = :inStock)
    """)
    fun searchProducts(
        @Param("name") name: String?,
        @Param("minPrice") minPrice: Long?,
        @Param("maxPrice") maxPrice: Long?,
        @Param("category") category: String?,
        @Param("inStock") inStock: Boolean?,
        pageable: Pageable
    ): Page<ProductEntity>
}
```

#### 3. 최적화된 Facade/Service 구현 (중기)

```kotlin
@Transactional(readOnly = true)
fun getProducts(criteria: ProductCriteria.Search, pageable: Pageable): Page<ProductResult.Product> {
    val productsPage = productJpaRepository.searchProducts(
        name = criteria.name,
        minPrice = criteria.minPrice,
        maxPrice = criteria.maxPrice,
        category = criteria.category,
        inStock = criteria.inStock,
        pageable = pageable
    )
    
    return productsPage.map { ProductResult.Product.from(ProductEntity.toDomain(it)) }
}
```

#### 4. 캐싱 적용 (장기)

```kotlin
@Service
@CacheConfig(cacheNames = ["productCache"])
class CachedProductService(
    private val productJpaRepository: ProductJpaRepository
) {
    /**
     * 인기 상품 목록을 조회합니다.
     * 결과는 캐시됩니다.
     */
    @Cacheable(key = "'popular_products_' + #pageable.pageNumber")
    fun getPopularProducts(pageable: Pageable): Page<ProductEntity> {
        return productJpaRepository.findByOrderByViewCountDesc(pageable)
    }
    
    /**
     * 카테고리별 상품을 조회합니다.
     * 결과는 캐시됩니다.
     */
    @Cacheable(key = "'category_' + #category + '_page_' + #pageable.pageNumber")
    fun getProductsByCategory(category: String, pageable: Pageable): Page<ProductEntity> {
        return productJpaRepository.findByCategory(category, pageable)
    }
    
    /**
     * 상품 정보 변경 시 캐시를 무효화합니다.
     */
    @CacheEvict(allEntries = true)
    fun invalidateCache() {
        // 캐시 무효화
    }
}
```

#### 5. 검색 엔진 통합 (장기)

```kotlin
@Service
class ElasticsearchProductService(
    private val elasticsearchClient: RestHighLevelClient
) {
    /**
     * Elasticsearch를 사용한 상품 검색
     */
    fun searchProducts(searchText: String, pageable: Pageable): SearchResult<Product> {
        val searchRequest = SearchRequest("products")
        val searchSourceBuilder = SearchSourceBuilder()
        
        val query = QueryBuilders.multiMatchQuery(searchText, "name", "description", "category")
            .fuzziness(Fuzziness.AUTO)
        
        searchSourceBuilder.query(query)
        searchSourceBuilder.from(pageable.pageNumber * pageable.pageSize)
        searchSourceBuilder.size(pageable.pageSize)
        
        searchRequest.source(searchSourceBuilder)
        
        val response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
        
        // 검색 결과 변환 로직
        val products = response.hits.map { hit -> 
            objectMapper.readValue(hit.sourceAsString, Product::class.java)
        }
        
        return SearchResult(
            products = products,
            totalHits = response.hits.totalHits?.value ?: 0,
            pageable = pageable
        )
    }
}
```

## 성능 개선 효과

### 쿠폰 사용자 정보 조회
1. **응답 시간 단축**
   - 페이지네이션을 통한 데이터 처리량 제한
   - 필터링을 통한 필요한 쿠폰만 조회

2. **시스템 부하 감소**
   - 사용 가능한 쿠폰만 선별적으로 조회
   - 필요한 정보만 로드하는 DTO 프로젝션 적용

3. **확장성 개선**
   - 데이터 증가에도 일정한 성능 유지
   - 요약 정보 테이블을 통한 집계 데이터 빠른 조회

### 상품 전체 조회
1. **응답 시간 단축**
   - 페이지네이션을 통한 데이터 처리량 제한
   - 캐싱을 통한 반복 요청 빠른 응답

2. **검색 성능 향상**
   - 최적화된 검색 쿼리
   - 전용 검색 엔진 도입으로 복잡한 검색 지원

3. **사용자 경험 개선**
   - 페이지네이션을 통한 점진적 데이터 로딩
   - 다양한 필터링 및 정렬 옵션 제공

## 구현 시 고려사항

### 쿠폰 사용자 정보 조회
1. **인덱스 최적화** (단기)
   - `CouponUserEntity.userId` 컬럼에 인덱스 추가
   - `CouponUserEntity.status`와 `CouponUserEntity.expiryDate`에 인덱스 추가

2. **API 설계 변경** (중기)
   - 쿠폰 상태별 필터링 제공
   - 사용자별 쿠폰 요약 정보 API 추가

3. **캐시 정책** (장기)
   - 사용자별 쿠폰 정보 캐싱
   - 쿠폰 발행/사용 시 관련 캐시 무효화

### 상품 전체 조회
1. **인덱스 최적화** (단기)
   - `ProductEntity.category`, `ProductEntity.price` 등 주요 필드에 인덱스 추가
   - 검색 및 정렬에 최적화된 복합 인덱스 고려

2. **캐싱 전략** (중기)
   - 인기 상품, 카테고리별 상품 등 자주 요청되는 목록 캐싱
   - 캐시 무효화 전략 (TTL 또는 상품 변경 시)

3. **검색 인프라 구축** (장기)
   - Elasticsearch 등 전문 검색 엔진 도입
   - 상품 데이터 인덱싱 및 동기화 전략 