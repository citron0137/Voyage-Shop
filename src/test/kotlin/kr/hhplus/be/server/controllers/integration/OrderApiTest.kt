package kr.hhplus.be.server.controllers.integration

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.controller.order.OrderRequest
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.domain.order.OrderException
import kr.hhplus.be.server.domain.product.ProductException
import kr.hhplus.be.server.domain.user.UserException
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
@DisplayName("주문 API 테스트")
@Transactional
class OrderApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    private lateinit var testUserId: String
    private lateinit var testProductId: String
    private lateinit var testCouponId: String
    
    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 생성
        val createUserResponse = mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()
        
        val userResponseContent = createUserResponse.response.contentAsString
        val userResponseObj = objectMapper.readValue(userResponseContent, BaseResponse::class.java)
        val userDataMap = userResponseObj.data as Map<*, *>
        testUserId = userDataMap["id"] as String
        
        // 테스트용 상품 생성
        val createProductRequest = mapOf(
            "name" to "테스트 상품",
            "price" to 10000L,
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
        testProductId = productDataMap["productId"] as String
        
        // 테스트용 쿠폰 이벤트 생성
        val createCouponEventRequest = mapOf(
            "benefitMethod" to "DISCOUNT_FIXED_AMOUNT",
            "benefitAmount" to "2000",
            "totalIssueAmount" to 10
        )
        
        val createCouponEventResponse = mockMvc.perform(
            post("/api/v1/coupon-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCouponEventRequest))
        ).andReturn()
        
        val couponEventResponseContent = createCouponEventResponse.response.contentAsString
        val couponEventResponseObj = objectMapper.readValue(couponEventResponseContent, BaseResponse::class.java)
        val couponEventDataMap = couponEventResponseObj.data as Map<*, *>
        val couponEventId = couponEventDataMap["id"] as String
        
        // 테스트용 쿠폰 발급
        val issueCouponRequest = mapOf(
            "userId" to testUserId
        )
        
        val issueCouponResponse = mockMvc.perform(
            post("/api/v1/coupon-events/${couponEventId}/issue-coupon-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueCouponRequest))
        ).andReturn()
        
        val couponResponseContent = issueCouponResponse.response.contentAsString
        val couponResponseObj = objectMapper.readValue(couponResponseContent, BaseResponse::class.java)
        val couponDataMap = couponResponseObj.data as Map<*, *>
        testCouponId = couponDataMap["couponUserId"] as String
    }
    
    @Nested
    @DisplayName("주문 생성 시")
    inner class CreateOrder {
        
        @Test
        @DisplayName("유효한 주문 정보로 요청하면 주문이 생성되어야 한다")
        fun withValidRequest_createsOrder() {
            // given
            val createOrderRequest = OrderRequest.Create(
                userId = testUserId,
                orderItemList = listOf(
                    OrderRequest.OrderItem(
                        productId = testProductId,
                        count = 2L
                    )
                ),
                payment = OrderRequest.Payment(
                    couponId = null
                )
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createOrderRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.userId").value(testUserId))
                .andExpect(jsonPath("$.data.paymentId").exists())
                .andExpect(jsonPath("$.data.totalAmount").value(20000)) // 10000 * 2
                .andExpect(jsonPath("$.data.totalDiscountAmount").value(0)) // 할인 없음
                .andExpect(jsonPath("$.data.finalAmount").value(20000)) // 할인 없음
                .andExpect(jsonPath("$.data.items").isArray)
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(testProductId))
                .andExpect(jsonPath("$.data.items[0].amount").value(2))
                .andExpect(jsonPath("$.data.items[0].unitPrice").value(10000))
                .andExpect(jsonPath("$.data.items[0].totalPrice").value(20000)) // 10000 * 2
        }
        
        @Test
        @DisplayName("쿠폰을 적용한 주문을 생성하면 할인이 적용되어야 한다")
        fun withCoupon_appliesDiscount() {
            // given
            val createOrderRequest = OrderRequest.Create(
                userId = testUserId,
                orderItemList = listOf(
                    OrderRequest.OrderItem(
                        productId = testProductId,
                        count = 2L
                    )
                ),
                payment = OrderRequest.Payment(
                    couponId = testCouponId
                )
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createOrderRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.userId").value(testUserId))
                .andExpect(jsonPath("$.data.paymentId").exists())
                .andExpect(jsonPath("$.data.totalAmount").value(20000)) // 10000 * 2
                .andExpect(jsonPath("$.data.totalDiscountAmount").value(2000)) // 쿠폰 할인
                .andExpect(jsonPath("$.data.finalAmount").value(18000)) // 20000 - 2000
                .andExpect(jsonPath("$.data.items").isArray)
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.discounts").isArray)
                .andExpect(jsonPath("$.data.discounts.length()").value(1))
                .andExpect(jsonPath("$.data.discounts[0].type").value("COUPON"))
                .andExpect(jsonPath("$.data.discounts[0].amount").value(2000))
        }
        
        @Test
        @DisplayName("존재하지 않는 상품 ID로 요청하면 에러가 반환되어야 한다")
        fun withNonExistentProductId_returnsError() {
            // given
            val nonExistentProductId = "non-existent-id"
            val createOrderRequest = OrderRequest.Create(
                userId = testUserId,
                orderItemList = listOf(
                    OrderRequest.OrderItem(
                        productId = nonExistentProductId,
                        count = 2L
                    )
                ),
                payment = OrderRequest.Payment(
                    couponId = null
                )
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createOrderRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"))
        }
        
        @Test
        @DisplayName("재고보다 많은 수량으로 요청하면 에러가 반환되어야 한다")
        fun withExcessiveQuantity_returnsError() {
            // given
            val createOrderRequest = OrderRequest.Create(
                userId = testUserId,
                orderItemList = listOf(
                    OrderRequest.OrderItem(
                        productId = testProductId,
                        count = 999L // 재고(100)보다 많은 수량
                    )
                ),
                payment = OrderRequest.Payment(
                    couponId = null
                )
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createOrderRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("PRODUCT_STOCK_UNDERFLOW"))
        }
        
        @Test
        @DisplayName("주문 항목이 없는 경우 에러가 반환되어야 한다")
        fun withNoOrderItems_returnsError() {
            // given
            val createOrderRequest = OrderRequest.Create(
                userId = testUserId,
                orderItemList = emptyList(),
                payment = OrderRequest.Payment(
                    couponId = null
                )
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createOrderRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("ORDER_FINAL_AMOUNT_INVALID"))
        }
    }
    
    @Nested
    @DisplayName("E2E 테스트 시나리오")
    inner class E2EScenario {
        
        @Test
        @DisplayName("상품 생성, 쿠폰 발급, 주문 생성 전체 흐름이 정상 동작해야 한다")
        fun createProductAndOrderWithCoupon_succeedsEndToEnd() {
            // 1. 새로운 상품 생성
            val createProductRequest = mapOf(
                "name" to "E2E 테스트 상품",
                "price" to 5000L,
                "stock" to 20L
            )
            
            val createProductResult = mockMvc.perform(
                post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createProductRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").exists())
                .andReturn()
            
            val productResponseContent = createProductResult.response.contentAsString
            val productResponseObj = objectMapper.readValue(productResponseContent, BaseResponse::class.java)
            val productDataMap = productResponseObj.data as Map<*, *>
            val newProductId = productDataMap["productId"] as String
            
            // 2. 새로운 쿠폰 이벤트 생성
            val createCouponEventRequest = mapOf(
                "benefitMethod" to "DISCOUNT_PERCENTAGE",
                "benefitAmount" to "10",
                "totalIssueAmount" to 5
            )
            
            val createCouponEventResult = mockMvc.perform(
                post("/api/v1/coupon-events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCouponEventRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn()
            
            val couponEventResponseContent = createCouponEventResult.response.contentAsString
            val couponEventResponseObj = objectMapper.readValue(couponEventResponseContent, BaseResponse::class.java)
            val couponEventDataMap = couponEventResponseObj.data as Map<*, *>
            val newCouponEventId = couponEventDataMap["id"] as String
            
            // 3. 쿠폰 발급
            val issueCouponRequest = mapOf(
                "userId" to testUserId
            )
            
            val issueCouponResult = mockMvc.perform(
                post("/api/v1/coupon-events/${newCouponEventId}/issue-coupon-user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(issueCouponRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.couponUserId").exists())
                .andReturn()
            
            val couponResponseContent = issueCouponResult.response.contentAsString
            val couponResponseObj = objectMapper.readValue(couponResponseContent, BaseResponse::class.java)
            val couponDataMap = couponResponseObj.data as Map<*, *>
            val newCouponUserId = couponDataMap["couponUserId"] as String
            
            // 4. 주문 생성
            val createOrderRequest = OrderRequest.Create(
                userId = testUserId,
                orderItemList = listOf(
                    OrderRequest.OrderItem(
                        productId = newProductId,
                        count = 4L
                    )
                ),
                payment = OrderRequest.Payment(
                    couponId = newCouponUserId
                )
            )
            
            // 주문 생성 요청
            mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createOrderRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.userId").value(testUserId))
                .andExpect(jsonPath("$.data.totalAmount").value(20000)) // 5000 * 4
                .andExpect(jsonPath("$.data.totalDiscountAmount").value(2000)) // 10% 할인
                .andExpect(jsonPath("$.data.finalAmount").value(18000)) // 20000 - 2000
                .andExpect(jsonPath("$.data.items").isArray)
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(newProductId))
                .andExpect(jsonPath("$.data.items[0].amount").value(4))
                .andExpect(jsonPath("$.data.discounts").isArray)
                .andExpect(jsonPath("$.data.discounts.length()").value(1))
                .andExpect(jsonPath("$.data.discounts[0].type").value("COUPON"))
                .andExpect(jsonPath("$.data.discounts[0].discountId").value(newCouponUserId))
                
            // 5. 상품 재고 확인
            mockMvc.perform(
                get("/api/v1/products/{productId}", newProductId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stock").value(16)) // 20 - 4
                
            // 6. 쿠폰 목록 조회하여 사용 여부 확인
            // 주: 실제 API에서 사용된 쿠폰 상태 확인 로직은 구현에 따라 다를 수 있음
            mockMvc.perform(
                get("/api/v1/coupon-users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + newCouponUserId + "')]").exists())
        }
    }
} 