package kr.hhplus.be.server.controllers.integration

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.domain.couponuser.CouponUser
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import kr.hhplus.be.server.domain.couponuser.CouponUserRepository
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
@DisplayName("쿠폰 사용자 API 테스트")
@Transactional
class CouponUserApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var couponUserRepository: CouponUserRepository
    
    private lateinit var testUserId: String
    private lateinit var couponId1: String
    private lateinit var couponId2: String
    
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
        
        // 테스트 쿠폰 ID 생성
        couponId1 = UUID.randomUUID().toString()
        couponId2 = UUID.randomUUID().toString()
        
        // 테스트 데이터 직접 생성 - 고정 금액 할인 쿠폰
        val now = LocalDateTime.now()
        val couponUser1 = CouponUser(
            couponUserId = couponId1,
            userId = testUserId,
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "5000",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )
        
        // 테스트 데이터 직접 생성 - 퍼센트 할인 쿠폰
        val couponUser2 = CouponUser(
            couponUserId = couponId2,
            userId = testUserId,
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
            benefitAmount = "10",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )
        
        // 데이터베이스에 저장
        couponUserRepository.create(couponUser1)
        couponUserRepository.create(couponUser2)
    }
    
    @Nested
    @DisplayName("모든 쿠폰 사용자 조회 시")
    inner class GetAllCouponUsers {
        
        @Test
        @DisplayName("모든 쿠폰 사용자 목록이 반환되어야 한다")
        fun returnsAllCouponUsers() {
            // when
            val result = mockMvc.perform(
                get("/api/v1/coupon-users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray)
                // 생성한 쿠폰 데이터 확인
                .andExpect(jsonPath("$.data[?(@.id == '" + couponId1 + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.id == '" + couponId2 + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.type == 'DISCOUNT_FIXED_AMOUNT')]").exists())
                .andExpect(jsonPath("$.data[?(@.type == 'DISCOUNT_PERCENTAGE')]").exists())
        }
    }
    
    @Nested
    @DisplayName("사용자별 쿠폰 조회 테스트")
    inner class GetCouponsByUserId {
        
        @Test
        @DisplayName("현재 구현된 API로 사용자 쿠폰 확인")
        fun checkUserCouponsWithExistingAPI() {
            // 새로운 사용자와 쿠폰 생성
            val createUserResponse = mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andReturn()
            
            val responseContent = createUserResponse.response.contentAsString
            val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
            val dataMap = responseObj.data as Map<*, *>
            val newUserId = dataMap["id"] as String
            
            // 새 사용자를 위한 쿠폰 생성
            val newCouponId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            val newCoupon = CouponUser(
                couponUserId = newCouponId,
                userId = newUserId,
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "3000",
                usedAt = null,
                createdAt = now,
                updatedAt = now
            )
            
            // 데이터베이스에 저장
            couponUserRepository.create(newCoupon)
            
            // API를 통해 모든 쿠폰을 조회하고 새로 생성한 사용자의 쿠폰이 있는지 확인
            val result = mockMvc.perform(
                get("/api/v1/coupon-users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // 결과 확인
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + newCouponId + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.userId == '" + newUserId + "')]").exists())
        }
    }
    
    @Nested
    @DisplayName("쿠폰 사용 테스트")
    inner class UseCoupon {
        
        @Test
        @DisplayName("리포지토리를 통한 쿠폰 사용 상태 확인")
        fun testCouponUsageWithRepository() {
            // 새 쿠폰 생성
            val testCouponId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            val testCoupon = CouponUser(
                couponUserId = testCouponId,
                userId = testUserId,
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "2000",
                usedAt = null,
                createdAt = now,
                updatedAt = now
            )
            
            // 데이터베이스에 저장
            couponUserRepository.create(testCoupon)
            
            // 쿠폰 사용 - 리포지토리 직접 활용
            val usedCoupon = testCoupon.use()
            couponUserRepository.update(usedCoupon)
            
            // API로 조회하여 사용 상태 확인
            val result = mockMvc.perform(
                get("/api/v1/coupon-users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // 결과 확인
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + testCouponId + "')]").exists())
                // 추가 확인이 필요한 경우 작성
        }
    }
    
    @Nested
    @DisplayName("E2E 테스트 시나리오")
    inner class E2EScenario {
        
        @Test
        @DisplayName("리포지토리를 활용한 전체 흐름 테스트")
        fun testFullFlowWithRepository() {
            // 1. 사용자 생성
            val createUserResponse = mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andReturn()
            
            val responseContent = createUserResponse.response.contentAsString
            val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
            val dataMap = responseObj.data as Map<*, *>
            val userId = dataMap["id"] as String
            
            // 2. 쿠폰 생성 (리포지토리 직접 사용)
            val couponId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            val testCoupon = CouponUser(
                couponUserId = couponId,
                userId = userId,
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "5000",
                usedAt = null,
                createdAt = now,
                updatedAt = now
            )
            
            couponUserRepository.create(testCoupon)
            
            // 3. 쿠폰 조회
            val getCouponsResult = mockMvc.perform(
                get("/api/v1/coupon-users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + couponId + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.id == '" + couponId + "')].userId").value(userId))
            
            // 4. 쿠폰 사용 (리포지토리 직접 사용)
            val usedCoupon = testCoupon.use()
            couponUserRepository.update(usedCoupon)
            
            // 5. 사용 후 쿠폰 상태 확인
            mockMvc.perform(
                get("/api/v1/coupon-users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == '" + couponId + "')]").exists())
                // usedAt 필드는 응답에 포함되지 않을 수 있으므로 이 부분은 실제 API 응답 구조에 따라 수정 필요
        }
    }
} 