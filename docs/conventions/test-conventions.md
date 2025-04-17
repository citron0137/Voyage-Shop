# 통합 테스트 컨벤션

## 1. 테스트 구조

### 패키지 구조
```
src/test/kotlin/kr/hhplus/be/server/
├── IntegrationTestBase.kt      # 테스트 기본 클래스
├── TestcontainersConfiguration.kt  # 테스트 컨테이너 설정
├── api/                        # API 엔드포인트 테스트
│   ├── user/                   # 사용자 API 테스트
│   ├── product/                # 상품 API 테스트
│   └── order/                  # 주문 API 테스트
├── integration/                # 시나리오 기반 통합 테스트
│   ├── order/                  # 주문 흐름 통합 테스트
│   └── payment/                # 결제 통합 테스트
├── application/                # 애플리케이션 서비스 테스트
└── domain/                     # 도메인 서비스 테스트
```

## 2. 네이밍 규칙

### 클래스 네이밍
- API 테스트: `{도메인명}ApiTest` (예: `UserApiTest`)
- 통합 테스트: `{도메인명}IntegrationTest` 또는 `{기능명}FlowIntegrationTest` (예: `OrderFlowIntegrationTest`)
- Facade 테스트: `{도메인명}FacadeTest` (예: `UserFacadeTest`)

### 메서드 네이밍
- `{테스트상황}_{기대결과}` 형식 권장 (예: `주문생성_포인트차감됨`)
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
- `IntegrationTestBase` 클래스를 상속하여 공통 설정 재사용
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

## 8. 동시성 테스트
- 스레드 풀을 활용한 동시성 이슈 테스트
- 락 메커니즘 검증에 활용

## 9. 테스트 샘플 코드

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