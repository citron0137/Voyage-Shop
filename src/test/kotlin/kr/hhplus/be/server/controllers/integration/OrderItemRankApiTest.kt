package kr.hhplus.be.server.controllers.integration

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.controller.order.OrderRequest
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@DisplayName("주문 상품 순위 API 테스트")
@Transactional
class OrderItemRankApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    private var testUserIds = mutableListOf<String>()
    private var testProductIds = mutableListOf<String>()
    
    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 3명 생성
        for (i in 1..3) {
            val createUserResponse = mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andReturn()
            
            val userResponseContent = createUserResponse.response.contentAsString
            val userResponseObj = objectMapper.readValue(userResponseContent, BaseResponse::class.java)
            val userDataMap = userResponseObj.data as Map<*, *>
            testUserIds.add(userDataMap["id"] as String)
        }
        
        // 테스트용 상품 5개 생성
        for (i in 1..5) {
            val createProductRequest = mapOf(
                "name" to "테스트 상품 $i",
                "price" to (1000L * i),
                "stock" to 100L
            )
            
            val createProductResponse = mockMvc.perform(
                post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createProductRequest))
            ).andReturn()
            
            val productResponseContent = createProductResponse.response.contentAsString
            val productResponseObj = objectMapper.readValue(productResponseContent, BaseResponse::class.java)
            val productDataMap = productResponseObj.data as Map<*, *>
            testProductIds.add(productDataMap["productId"] as String)
        }
    }
    
    @Nested
    @DisplayName("주문 상품 순위 조회 시")
    inner class GetOrderItemRank {
        
        @Test
        @DisplayName("주문이 없는 경우 빈 배열이 반환되어야 한다")
        fun withNoOrders_returnsEmptyArray() {
            // when
            val result = mockMvc.perform(
                get("/api/v1/order-item-rank")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(0))
        }
        
        @Test
        @DisplayName("주문이 있는 경우 주문 횟수가 많은 순으로 정렬된 상품 목록이 반환되어야 한다")
        fun withOrders_returnsProductsOrderedByOrderCount() {
            // given
            // 상품별 주문 횟수를 다르게 설정하여 순위가 생기도록 함
            // 상품1: 3번 주문, 상품2: 2번 주문, 상품3: 1번 주문, 상품4와 5: 주문 없음
            
            // 첫 번째 사용자: 상품1, 상품2 주문
            createOrder(testUserIds[0], mapOf(
                testProductIds[0] to 1L,
                testProductIds[1] to 1L
            ))
            
            // 두 번째 사용자: 상품1, 상품3 주문
            createOrder(testUserIds[1], mapOf(
                testProductIds[0] to 1L,
                testProductIds[2] to 1L
            ))
            
            // 세 번째 사용자: 상품1, 상품2 주문
            createOrder(testUserIds[2], mapOf(
                testProductIds[0] to 1L,
                testProductIds[1] to 1L
            ))
            
            // when
            val result = mockMvc.perform(
                get("/api/v1/order-item-rank")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(3)) // 3개 상품에 대한 주문이 있었음
                .andExpect(jsonPath("$.data[0].productId").value(testProductIds[0])) // 가장 많이 주문된 상품
                .andExpect(jsonPath("$.data[0].orderCount").value(3)) // 3번 주문됨
                .andExpect(jsonPath("$.data[1].productId").value(testProductIds[1])) // 두 번째로 많이 주문된 상품
                .andExpect(jsonPath("$.data[1].orderCount").value(2)) // 2번 주문됨
                .andExpect(jsonPath("$.data[2].productId").value(testProductIds[2])) // 세 번째로 많이 주문된 상품
                .andExpect(jsonPath("$.data[2].orderCount").value(1)) // 1번 주문됨
        }
        
        @Test
        @DisplayName("다량 주문시 주문 수량이 많은 상품이 상위에 랭크되어야 한다")
        fun withLargeOrderQuantities_ranksProductsByTotalQuantity() {
            // given
            // 상품1: 1번 주문 (수량 5개)
            // 상품2: 2번 주문 (각 수량 2개씩, 총 4개)
            // 상품3: 3번 주문 (각 수량 1개씩, 총 3개)
            
            // 첫 번째 사용자: 상품1 5개 주문
            createOrder(testUserIds[0], mapOf(
                testProductIds[0] to 5L
            ))
            
            // 두 번째 사용자: 상품2 2개, 상품3 1개 주문
            createOrder(testUserIds[1], mapOf(
                testProductIds[1] to 2L,
                testProductIds[2] to 1L
            ))
            
            // 세 번째 사용자: 상품2 2개, 상품3 2개 주문
            createOrder(testUserIds[2], mapOf(
                testProductIds[1] to 2L,
                testProductIds[2] to 2L
            ))
            
            // when
            val result = mockMvc.perform(
                get("/api/v1/order-item-rank")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].productId").value(testProductIds[0])) // 총 5개 주문
                .andExpect(jsonPath("$.data[0].orderCount").value(5))
                .andExpect(jsonPath("$.data[1].productId").value(testProductIds[1])) // 총 4개 주문
                .andExpect(jsonPath("$.data[1].orderCount").value(4))
                .andExpect(jsonPath("$.data[2].productId").value(testProductIds[2])) // 총 3개 주문
                .andExpect(jsonPath("$.data[2].orderCount").value(3))
        }
        
        @Test
        @DisplayName("5개 이상의 상품에 주문이 있더라도 상위 5개만 반환되어야 한다")
        fun withMoreThan5Products_returnsTop5Only() {
            // given
            // 모든 상품에 주문을 생성하되, 순서를 뒤바꿔 인덱스가 높은 상품이 더 많이 주문되도록 함
            for (i in 0 until testProductIds.size) {
                val orderQuantity = testProductIds.size - i // 5, 4, 3, 2, 1 순으로 주문량 설정
                createOrder(testUserIds[0], mapOf(
                    testProductIds[i] to orderQuantity.toLong()
                ))
            }
            
            // when
            val result = mockMvc.perform(
                get("/api/v1/order-item-rank")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(5)) // 최대 5개만 반환
                .andExpect(jsonPath("$.data[0].productId").value(testProductIds[0])) // 5개 주문
                .andExpect(jsonPath("$.data[0].orderCount").value(5))
                .andExpect(jsonPath("$.data[1].productId").value(testProductIds[1])) // 4개 주문
                .andExpect(jsonPath("$.data[1].orderCount").value(4))
                .andExpect(jsonPath("$.data[2].productId").value(testProductIds[2])) // 3개 주문
                .andExpect(jsonPath("$.data[2].orderCount").value(3))
                .andExpect(jsonPath("$.data[3].productId").value(testProductIds[3])) // 2개 주문
                .andExpect(jsonPath("$.data[3].orderCount").value(2))
                .andExpect(jsonPath("$.data[4].productId").value(testProductIds[4])) // 1개 주문
                .andExpect(jsonPath("$.data[4].orderCount").value(1))
        }
    }
    
    /**
     * 헬퍼 메서드: 주문 생성
     */
    private fun createOrder(userId: String, productQuantities: Map<String, Long>) {
        val orderItems = productQuantities.map { (productId, quantity) ->
            OrderRequest.OrderItem(
                productId = productId,
                count = quantity
            )
        }
        
        val createOrderRequest = OrderRequest.Create(
            userId = userId,
            orderItemList = orderItems,
            payment = OrderRequest.Payment(
                couponId = null
            )
        )
        
        mockMvc.perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest))
        )
    }
} 