# 새로운 컨벤션에 대한 고민

## 1. 너무 보일러플레이트가 많아지는 것 같습니다.
어떻게 해결하면 좋을까요?

### 1.1 커서의 답변

종단간 관심사(컨트롤러→파사드→애플리케이션→도메인) 구조에서 발생하는 보일러플레이트 코드를 효과적으로 줄이기 위한 구체적인 방안은 다음과 같습니다:

#### 파사드 레이어 선택적 적용을 위한 명확한 기준

파사드 레이어는 다음 경우에만 적용합니다:

- 여러 애플리케이션 서비스를 조합하는 경우
- 동일한 구성의 서비스 호출이 여러 컨트롤러에서 반복되는 경우

단일 애플리케이션 서비스만 호출하는 단순 API는 컨트롤러에서 애플리케이션 서비스를 직접 호출합니다:

```kotlin
// 파사드 없이 컨트롤러에서 직접 애플리케이션 서비스 호출
@RestController
class ProductController(
    private val productService: ProductService  // 애플리케이션 서비스
) : ProductControllerApi {
    
    override fun getProduct(productId: String): BaseResponse<ProductResponse> {
        val criteria = ProductCriteria.GetById(productId)
        val product = productService.getProduct(criteria)
        return BaseResponse.success(ProductResponse.from(product))
    }
}
```

#### 계층 간 데이터 변환 최소화

엔드투엔드 데이터 흐름에서 중복 매핑을 줄입니다:

1. **직접 변환 활용**: 필요한 경우 중간 계층을 건너뛰는 변환 메서드 제공

```kotlin
// Controller → Application 직접 변환
fun toServiceCriteria(request: ProductRequest.Create): ProductCriteria.Create {
    return ProductCriteria.Create(
        name = request.name,
        price = request.price
    )
}

// Application → Controller 직접 변환
fun ProductResult.toResponse(): ProductResponse {
    return ProductResponse(
        id = this.id,
        name = this.name,
        price = this.price
    )
}
```

2. **변환 최소화 규약**: 계층 간 데이터 구조가 동일하면 중복 변환 클래스 작성하지 않기

```kotlin
// 동일한 구조는 재사용
typealias ProductFacadeCriteria = ProductCriteria
typealias ProductFacadeResult = ProductResult
```

#### 인라인 기준 객체 사용

단순한 케이스에서는 별도 클래스 대신 파라미터를 직접 사용:

```kotlin
// 복잡한 Criteria 객체 대신
fun getProductById(productId: String): ProductResult {
    return productDomainService.getProduct(ProductCommand.GetById(productId))
}

// 파라미터 여러 개를 갖는 메서드 대신 data class 활용
fun updateProductPrice(productId: String, newPrice: Long): ProductResult {
    return productDomainService.updatePrice(ProductCommand.UpdatePrice(productId, newPrice))
}
```

#### 함수형 파이프라인 패턴 도입

연속된 데이터 변환 단계를 함수형 파이프라인으로 구성:

```kotlin
fun createProduct(request: ProductRequest.Create): BaseResponse<ProductResponse> {
    return request
        .let(::toCriteria)                 // Request → Criteria 변환
        .let(productService::createProduct) // 서비스 호출
        .let(::toResponse)                 // Result → Response 변환
        .let(BaseResponse::success)        // 응답 래핑
}
```

#### 핵심 비즈니스 로직만 구현하는 집중 전략

애플리케이션의 모든 부분을 엄격한 계층 구조로 구현하지 않고, 복잡도에 따라 유연하게 적용:

1. **복잡한 핵심 도메인**: 완전한 계층 구조 적용 (컨트롤러→파사드→애플리케이션→도메인)
2. **단순 CRUD 영역**: 간소화된 계층 구조 적용 (컨트롤러→애플리케이션→도메인)
3. **설정/관리 기능**: 최소 계층 구조 적용 (컨트롤러→도메인 또는 컨트롤러→인프라)

#### 코드 생성 도구 활용

자주 반복되는 패턴에 대한 코드 생성 도구를 개발:

1. **IDE 템플릿**: IntelliJ의 Live Templates 기능을 활용하여 반복적인 코드 구조를 빠르게 생성
2. **템플릿 기반 코드 생성기**: 엔드포인트 정의에서 기본 API 엔드투엔드 코드 생성

#### 결론

보일러플레이트 코드 문제는 파사드를 선택적으로 적용하고, 데이터 변환을 최소화하며, 코틀린의 함수형 특성을 활용하여 상당히 개선할 수 있습니다. 중요한 것은 엄격한 계층 분리가 목적이 아니라 코드의 명확성과 유지보수성을 높이는 것이 목적임을 인식하고, 상황에 맞게 유연하게 적용하는 것입니다.

### 1.3. 복잡한 조회로직에 대해서 어플리케이션레이어에서 entityManager나 도메인레이어의 repository를 직접 호출하는 것을 허용해주는 것은 어떻게 생각해?

애플리케이션 레이어에서 Repository를 직접 호출하는 접근 방식에 대해 균형 잡힌 관점을 제시하겠습니다:

#### 장점

1. **성능 최적화**: 복잡한 조회 로직의 경우, 도메인 서비스를 거치지 않고 Repository나 EntityManager를 직접 사용하면 불필요한 객체 변환과 메서드 호출을 줄일 수 있습니다.

2. **복잡한 쿼리 지원**: JPA의 Criteria API, JPQL, QueryDSL 등을 활용한 복잡한 쿼리는 애플리케이션 레이어에서 직접 구성하는 것이 더 자연스러울 수 있습니다.

3. **READ 연산의 분리**: 읽기 작업은 상태 변경이 없으므로, 도메인 로직을 우회하더라도 도메인 무결성을 해치지 않습니다.

4. **프로젝션 유연성**: DTO 직접 조회 등 도메인 엔티티가 아닌 형태의 데이터 프로젝션이 필요할 때 Repository 직접 접근이 효율적입니다.

#### 단점

1. **아키텍처 일관성 저하**: 레이어 간 책임 분리 원칙이 약화되어 아키텍처의 일관성이 저하될 수 있습니다.

2. **도메인 로직 우회**: Repository 직접 접근은 도메인 로직을 우회할 위험이 있으며, 이는 장기적으로 비즈니스 규칙의 분산으로 이어질 수 있습니다.

3. **테스트 복잡성**: 애플리케이션 서비스 테스트 시 Repository 모킹이 필요해져 테스트 구성이 복잡해질 수 있습니다.

#### 균형 잡힌 접근법

순수 아키텍처 원칙과 실용성 사이의 균형을 위해 다음과 같은 가이드라인을 제안합니다:

1. **"읽기 전용" 원칙**: Repository 직접 접근은 읽기 전용(조회) 작업으로 제한하고, 상태 변경 작업은 항상 도메인 서비스를 통해서만 수행합니다.

2. **Repository 인터페이스 확장**: 복잡한 조회 로직이 필요한 경우, 도메인 레이어의 Repository 인터페이스에 해당 메서드를 추가하는 것을 우선 고려합니다.

```kotlin
// 도메인 레이어의 Repository 인터페이스에 복잡한 조회 메서드 추가
interface ProductRepository {
    fun findById(id: String): Product?
    fun save(product: Product): Product
    
    // 복잡한 조회 메서드 추가
    fun findProductsWithCategoryAndReviewStats(criteria: ProductSearchCriteria): List<ProductWithStats>
}
```

3. **전용 쿼리 서비스 도입**: 매우 복잡한 조회 로직은 별도의 `QueryService`나 `ReadService` 클래스로 분리하여 관리합니다.

```kotlin
@Service
class ProductQueryService(
    private val entityManager: EntityManager,
    private val jdbcTemplate: JdbcTemplate
) {
    fun findProductAnalytics(criteria: AnalyticsCriteria): List<ProductAnalytics> {
        // 복잡한 쿼리 로직
    }
}
```

4. **명시적인 규약화**: 애플리케이션 레이어에서 Repository 직접 접근이 허용되는 상황과 제약 조건을 명확히 문서화합니다.

5. **CQRS 패턴 고려**: 읽기(Query)와 쓰기(Command) 책임을 명확히 분리하는 CQRS 패턴 도입을 검토합니다.

#### 결론

복잡한 조회 로직에 대해 애플리케이션 레이어에서 Repository를 직접 호출하는 것은, 읽기 전용 작업으로 제한하고 명확한 가이드라인을 설정한다면 실용적인 접근법이 될 수 있습니다. 다만, 이러한 예외는 성능이나 복잡성으로 인해 불가피한 경우로 제한하고, 가능한 한 도메인 레이어의 Repository 인터페이스를 확장하는 방향으로 먼저 접근하는 것이 바람직합니다.

특히, EntityManager를 직접 사용하는 것보다 도메인 Repository를 사용하는 것이 아키텍처 일관성 유지, 도메인 규칙 준수, 테스트 용이성 측면에서 더 바람직합니다. EntityManager는 JPA라는 특정 기술에 직접적으로 의존하게 되어 기술적 세부사항이 애플리케이션 레이어로 누출되는 문제가 있습니다. 정말 특별한 성능 최적화가 필요한 경우에만 별도의 QueryService를 통해 EntityManager를 제한적으로 사용하고, 일반적인 경우에는 도메인 Repository 인터페이스를 확장하여 사용하는 것이 권장됩니다.