# 테스트 컨벤션

## 1. 테스트 구조

### 패키지 구조
```
src/test/kotlin/kr/hhplus/be/server/
├── ServerApplicationTests.kt    # 스프링 부트 애플리케이션 컨텍스트 로드 테스트
├── TestcontainersConfiguration.kt  # 테스트 컨테이너 설정
├── api/                        # API 엔드포인트 테스트
│   ├── user/                   # 사용자 API 테스트
│   ├── product/                # 상품 API 테스트
│   └── ...                     # 기타 API 테스트
├── integration/                # 시나리오 기반 통합 테스트
│   ├── order/                  # 주문 흐름 통합 테스트
│   └── payment/                # 결제 통합 테스트
├── application/                # 애플리케이션 서비스 테스트
│   ├── user/                   # 사용자 애플리케이션 서비스 테스트
│   ├── order/                  # 주문 애플리케이션 서비스 테스트
│   └── ...                     # 기타 애플리케이션 서비스 테스트
└── domain/                     # 도메인 서비스 테스트
    ├── user/                   # 사용자 도메인 서비스 테스트
    ├── order/                  # 주문 도메인 서비스 테스트
    └── ...                     # 기타 도메인 서비스 테스트
```

## 2. 네이밍 규칙

### 클래스 네이밍
- API 테스트: `{도메인명}ApiTest` (예: `UserApiTest`)
- 통합 테스트: `{도메인명}IntegrationTest` 또는 `{기능명}FlowIntegrationTest` (예: `OrderFlowIntegrationTest`)
- 애플리케이션 서비스 테스트: `{도메인명}ApplicationServiceTest` (예: `UserApplicationServiceTest`)
- 도메인 서비스 테스트: `{도메인명}ServiceUnitTest` (예: `OrderServiceUnitTest`)
- Facade 테스트: `{도메인명}FacadeTest` (예: `UserFacadeTest`)

### 메서드 네이밍
- BDD 스타일: `` `{테스트상황}` `` 형식 권장 (예: `` `주문을 생성할 수 있다` ``)
- 또는 `{테스트상황}_{기대결과}` 형식 (예: `주문생성_포인트차감됨`)
- 또는 `{행위}Test` 형식 (예: `createUserTest`)

## 3. 테스트 구조
```kotlin
@Test
@DisplayName("테스트 설명")
fun testMethod() {
    // given: 테스트 사전 조건 설정
    
    // when: 테스트 대상 메서드 실행
    
    // then: 결과 검증
}
```

## 4. 공통 설정
- 공통 설정의 재사용을 위해 Base 클래스 상속 또는 설정 클래스 임포트
- TestContainers를 활용한 데이터베이스 통합 테스트

## 5. 테스트 격리
- `@Transactional` 어노테이션을 사용하여 각 테스트 메서드별 트랜잭션 롤백
- 테스트 케이스 간 독립성 보장을 위해 `@BeforeEach`에서 테스트 데이터 초기화

## 6. API 테스트
- MockMvc를 활용한 HTTP 요청 검증
- `@AutoConfigureMockMvc` 어노테이션 사용
- 상태 코드 및 응답 JSON 검증

## 7. 시나리오 기반 테스트
- 여러 서비스/컴포넌트를 연계한 비즈니스 흐름 테스트
- 흐름별로 given-when-then 주석 명확하게 구분
- 통합 테스트에서는 주로 성공 케이스(Happy Path)를 테스트하고, 예외 케이스는 단위 테스트에서 다루는 것을 권장
- 통합 테스트는 application layer 단위로 수행하는 것이 권장됨
  - 단일 도메인 로직은 domain/ 폴더에서 단위 테스트로 검증
  - 단일 애플리케이션 서비스는 application/ 폴더에서 테스트
  - 여러 도메인과 서비스를 아우르는 시나리오는 integration/ 폴더에서 테스트
- 통합 테스트 범위는 명확한 비즈니스 시나리오에 집중하고, 너무 많은 도메인을 한 번에 테스트하지 않도록 주의
  - 좋은 예: 주문생성-결제 흐름, 사용자등록-이메일인증 흐름
  - 나쁜 예: 사용자생성-포인트충전-쿠폰발급-주문생성-결제 등 너무 많은 단계를 포함하는 테스트

## 8. 동시성 테스트
- 스레드 풀을 활용한 동시성 이슈 테스트
- 락 메커니즘 검증에 활용

## 9. 단위 테스트
- Mockk를 활용한 의존성 모킹
- 도메인 로직에 집중한 테스트 작성

## 10. 테스트 샘플 코드

### API 테스트 예시

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class UserApiTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("사용자 생성 API 테스트")
    fun createUserTest() {
        // when & then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").exists())
    }
}
```

### 통합 테스트 예시

```kotlin
@SpringBootTest
@Import(TestcontainersConfiguration::class)
@Transactional
class OrderFlowIntegrationTest {

    @Autowired
    private lateinit var userFacade: UserFacade

    @Autowired
    private lateinit var orderService: OrderService

    @Test
    @DisplayName("주문 생성-결제-완료 통합 테스트")
    fun orderCreationToCompletionTest() {
        // given: 주문 생성에 필요한 데이터
        val user = userFacade.createUser()
        val testUserId = user.userId
        
        // when: 주문 생성
        val order = orderService.createOrder(/*주문 생성 파라미터*/)
        
        // then: 주문이 생성되었는지 확인
        assert(order.orderId.isNotEmpty())
        
        // when: 주문 결제
        val paymentResult = orderService.payOrder(order.orderId)
        
        // then: 결제가 성공했는지 확인
        assert(paymentResult.success)
    }
}

### 도메인 서비스 단위 테스트 예시

```kotlin
class OrderServiceUnitTest {
    private lateinit var orderRepository: OrderRepository
    private lateinit var orderItemRepository: OrderItemRepository
    private lateinit var orderDiscountRepository: OrderDiscountRepository
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        orderRepository = mockk()
        orderItemRepository = mockk()
        orderDiscountRepository = mockk()
        orderService = OrderService(orderRepository, orderItemRepository, orderDiscountRepository)
    }

    @Test
    fun `주문을 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        val paymentId = "test-payment-id"
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 2,
                unitPrice = 1000
            )
        )
        val command = OrderCommand.Create(userId, paymentId, orderItems)
        
        val expectedOrder = Order(
            orderId = "test-order-id",
            userId = userId,
            paymentId = paymentId,
            totalAmount = 2000,
            totalDiscountAmount = 0,
            finalAmount = 2000
        )
        
        every { orderRepository.create(any()) } returns expectedOrder
        every { orderItemRepository.createAll(any()) } returns listOf()
        
        // when
        val result = orderService.createOrder(command)

        // then
        verify { orderRepository.create(any()) }
        verify { orderItemRepository.createAll(any()) }
        assertEquals(expectedOrder.orderId, result.orderId)
        assertEquals(expectedOrder.totalAmount, result.totalAmount)
    }
} 