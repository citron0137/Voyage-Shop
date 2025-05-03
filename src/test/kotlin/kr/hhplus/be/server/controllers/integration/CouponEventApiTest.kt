package kr.hhplus.be.server.controllers.integration

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.domain.couponuser.CouponUserRepository
import kr.hhplus.be.server.domain.couponevent.CouponEvent
import kr.hhplus.be.server.domain.couponevent.CouponEventBenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEventRepository
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
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@DisplayName("쿠폰 이벤트 API 테스트")
@Transactional
class CouponEventApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var couponEventRepository: CouponEventRepository
    
    @Autowired
    private lateinit var couponUserRepository: CouponUserRepository
    
    private lateinit var testUserId: String
    private lateinit var couponEventId1: String
    private lateinit var couponEventId2: String
    
    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 생성
        val createUserResponse = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()
        
        val responseContent = createUserResponse.response.contentAsString
        val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
        val dataMap = responseObj.data as Map<*, *>
        testUserId = dataMap["id"] as String
        
        // 테스트 이벤트 ID 생성
        couponEventId1 = UUID.randomUUID().toString()
        couponEventId2 = UUID.randomUUID().toString()
        
        // 테스트 데이터 직접 생성 - 고정 금액 할인 쿠폰 이벤트
        val now = LocalDateTime.now()
        val couponEvent1 = CouponEvent(
            id = couponEventId1,
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "5000",
            totalIssueAmount = 100,
            leftIssueAmount = 100,
            createdAt = now,
            updatedAt = now
        )
        
        // 테스트 데이터 직접 생성 - 퍼센트 할인 쿠폰 이벤트
        val couponEvent2 = CouponEvent(
            id = couponEventId2,
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_PERCENTAGE,
            benefitAmount = "10",
            totalIssueAmount = 50,
            leftIssueAmount = 50,
            createdAt = now,
            updatedAt = now
        )
        
        // 데이터베이스에 저장
        couponEventRepository.create(couponEvent1)
        couponEventRepository.create(couponEvent2)
    }
    
    @Nested
    @DisplayName("쿠폰 이벤트 생성 시")
    inner class CreateCouponEvent {
        
        @Test
        @DisplayName("유효한 정보로 요청하면 쿠폰 이벤트가 생성되어야 한다")
        fun withValidRequest_createsCouponEvent() {
            // given
            val createRequest = mapOf(
                "benefitMethod" to "DISCOUNT_FIXED_AMOUNT",
                "benefitAmount" to "3000",
                "totalIssueAmount" to 200
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/coupon-events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.benefitMethod").value("DISCOUNT_FIXED_AMOUNT"))
                .andExpect(jsonPath("$.data.benefitAmount").value("3000"))
                .andExpect(jsonPath("$.data.totalIssueAmount").value(200))
                .andExpect(jsonPath("$.data.leftIssueAmount").value(200))
        }
    }
    
    @Nested
    @DisplayName("모든 쿠폰 이벤트 조회 시")
    inner class GetAllCouponEvents {
        
        @Test
        @DisplayName("모든 쿠폰 이벤트 목록이 반환되어야 한다")
        fun returnsAllCouponEvents() {
            // when
            val result = mockMvc.perform(
                get("/api/v1/coupon-events")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray)
                // 생성한 쿠폰 이벤트 데이터 확인
                .andExpect(jsonPath("$.data[?(@.id == '" + couponEventId1 + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.id == '" + couponEventId2 + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.benefitMethod == 'DISCOUNT_FIXED_AMOUNT')]").exists())
                .andExpect(jsonPath("$.data[?(@.benefitMethod == 'DISCOUNT_PERCENTAGE')]").exists())
        }
    }
    
    @Nested
    @DisplayName("쿠폰 발급 테스트")
    inner class IssueCoupon {
        
        @Test
        @DisplayName("유효한 이벤트 ID와 사용자 ID로 요청하면 쿠폰이 발급되어야 한다")
        fun withValidEventAndUser_issuesCoupon() {
            // given
            val issueRequest = mapOf(
                "userId" to testUserId
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/coupon-events/${couponEventId1}/issue-coupon-user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(issueRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.couponUserId").exists())
            
            // 쿠폰 이벤트의 재고가 감소했는지 확인
            val updatedEvent = couponEventRepository.findById(couponEventId1)
            assert(updatedEvent != null)
            assert(updatedEvent!!.leftIssueAmount == 99L)
        }
        
        @Test
        @DisplayName("존재하지 않는 이벤트 ID로 요청하면 에러 코드가 반환되어야 한다")
        fun withNonExistentEventId_returnsError() {
            // given
            val nonExistentEventId = "non-existent-id"
            val issueRequest = mapOf(
                "userId" to testUserId
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/coupon-events/${nonExistentEventId}/issue-coupon-user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(issueRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("COUPON_EVENT_NOT_FOUND"))
        }
    }
    
    @Nested
    @DisplayName("재고 소진 테스트")
    inner class StockDepletion {
        
        @Test
        @DisplayName("재고가 소진된 이벤트에 쿠폰 발급 요청하면 재고 소진 에러 코드가 반환되어야 한다")
        fun whenStockDepleted_returnsOutOfStockError() {
            // given: 재고 0인 이벤트 생성
            val zeroStockEventId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            val zeroStockEvent = CouponEvent(
                id = zeroStockEventId,
                benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "2000",
                totalIssueAmount = 5,
                leftIssueAmount = 0, // 재고 0
                createdAt = now,
                updatedAt = now
            )
            
            couponEventRepository.create(zeroStockEvent)
            
            // 발급 요청
            val issueRequest = mapOf(
                "userId" to testUserId
            )
            
            // when
            val result = mockMvc.perform(
                post("/api/v1/coupon-events/${zeroStockEventId}/issue-coupon-user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(issueRequest))
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("COUPON_EVENT_OUT_OF_STOCK"))
        }
    }
    
    @Nested
    @DisplayName("E2E 테스트 시나리오")
    inner class E2EScenario {
        
        @Test
        @DisplayName("쿠폰 이벤트 생성부터 쿠폰 발급까지 전체 흐름이 정상 동작해야 한다")
        fun createEventAndIssueCoupon_succeedsEndToEnd() {
            // 1. 쿠폰 이벤트 생성
            val createEventRequest = mapOf(
                "benefitMethod" to "DISCOUNT_PERCENTAGE",
                "benefitAmount" to "15",
                "totalIssueAmount" to 10
            )
            
            val createEventResult = mockMvc.perform(
                post("/api/v1/coupon-events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createEventRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn()
            
            // 생성된 이벤트 ID 추출
            val createEventResponse = createEventResult.response.contentAsString
            val eventResponseObj = objectMapper.readValue(createEventResponse, BaseResponse::class.java)
            val eventData = eventResponseObj.data as Map<*, *>
            val newEventId = eventData["id"] as String
            
            // 2. 이벤트 목록 조회해서 생성된 이벤트 확인
            mockMvc.perform(
                get("/api/v1/coupon-events")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + newEventId + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.id == '" + newEventId + "')].benefitMethod").value("DISCOUNT_PERCENTAGE"))
                .andExpect(jsonPath("$.data[?(@.id == '" + newEventId + "')].benefitAmount").value("15"))
                .andExpect(jsonPath("$.data[?(@.id == '" + newEventId + "')].totalIssueAmount").value(10))
                .andExpect(jsonPath("$.data[?(@.id == '" + newEventId + "')].leftIssueAmount").value(10))
            
            // 3. 쿠폰 발급 요청
            val issueRequest = mapOf(
                "userId" to testUserId
            )
            
            val issueCouponResult = mockMvc.perform(
                post("/api/v1/coupon-events/${newEventId}/issue-coupon-user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(issueRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.couponUserId").exists())
                .andReturn()
            
            // 발급된 쿠폰 ID 추출
            val issueCouponResponse = issueCouponResult.response.contentAsString
            val couponResponseObj = objectMapper.readValue(issueCouponResponse, BaseResponse::class.java)
            val couponData = couponResponseObj.data as Map<*, *>
            val newCouponUserId = couponData["couponUserId"] as String
            
            // 4. 이벤트 목록 다시 조회하여 재고 감소 확인
            mockMvc.perform(
                get("/api/v1/coupon-events")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + newEventId + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.id == '" + newEventId + "')].leftIssueAmount").value(9))
            
            // 5. 쿠폰 목록 조회하여 발급된 쿠폰 확인
            mockMvc.perform(
                get("/api/v1/coupon-users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + newCouponUserId + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.id == '" + newCouponUserId + "')].userId").value(testUserId))
                .andExpect(jsonPath("$.data[?(@.id == '" + newCouponUserId + "')].type").value("DISCOUNT_PERCENTAGE"))
        }
    }
} 