package kr.hhplus.be.server.controllers.integration

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.controller.userpoint.UserPointRequest
import kr.hhplus.be.server.domain.user.UserRepository
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
import org.springframework.web.servlet.resource.NoResourceFoundException

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@DisplayName("사용자 포인트 API 테스트")
@Transactional
class UserPointApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    private lateinit var userId: String
    
    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 생성
        val createResponse = mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()
        
        val responseContent = createResponse.response.contentAsString
        val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
        val dataMap = responseObj.data as Map<*, *>
        userId = dataMap["id"] as String
    }
    
    @Nested
    @DisplayName("사용자 포인트 조회 시")
    inner class UserPointRetrieval {
        
        @Test
        @DisplayName("유효한 사용자 ID로 조회하면 응답이 반환되어야 한다")
        fun withValidUserId_returnsResponse() {
            mockMvc.perform(
                get("/api/v1/user-points/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.amount").exists())
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andExpect(jsonPath("$.data.updatedAt").exists())
        }
        
        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회하면 응답이 반환되어야 한다")
        fun withNonExistentUserId_returnsResponse() {
            mockMvc.perform(
                get("/api/v1/user-points/{userId}", "non-existent-id")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.error.code").exists())
            .andExpect(jsonPath("$.error.message").exists())
        }
    }
    
    @Nested
    @DisplayName("포인트 충전 시")
    inner class PointCharge {
        
        @Test
        @DisplayName("유효한 금액으로 충전하면 응답이 반환되어야 한다")
        fun withValidAmount_returnsResponse() {
            // given
            val chargeRequest = UserPointRequest.Charge(amount = 1000)
            
            // when & then
            mockMvc.perform(
                post("/api/v1/user-points/{userId}/charge", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.amount").exists())
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andExpect(jsonPath("$.data.updatedAt").exists())
        }
        
        @Test
        @DisplayName("0 이하의 금액으로 충전하면 응답이 반환되어야 한다")
        fun withInvalidAmount_returnsResponse() {
            // given
            val chargeRequest = UserPointRequest.Charge(amount = 0)
            
            // when & then
            mockMvc.perform(
                post("/api/v1/user-points/{userId}/charge", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.error.code").exists())
            .andExpect(jsonPath("$.error.message").exists())
        }
        
        @Test
        @DisplayName("존재하지 않는 사용자 ID로 충전하면 응답이 반환되어야 한다")
        fun withNonExistentUserId_returnsResponse() {
            // given
            val chargeRequest = UserPointRequest.Charge(amount = 1000)
            
            // when & then
            mockMvc.perform(
                post("/api/v1/user-points/{userId}/charge", "non-existent-id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.error.code").exists())
            .andExpect(jsonPath("$.error.message").exists())
        }
    }
    
    @Nested
    @DisplayName("E2E 테스트 시나리오")
    inner class E2EScenario {
        
        @Test
        @DisplayName("포인트 충전 및 조회 흐름이 정상 동작해야 한다")
        fun chargeAndRetrievePoint_succeedsEndToEnd() {
            // 1. 포인트 조회
            val initialPointResponse = mockMvc.perform(
                get("/api/v1/user-points/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.amount").exists())
            .andReturn()
            
            // 초기 포인트 값 확인
            val initialResponseContent = initialPointResponse.response.contentAsString
            val initialResponseObj = objectMapper.readValue(initialResponseContent, BaseResponse::class.java)
            val initialDataMap = initialResponseObj.data as Map<*, *>
            val initialAmount = initialDataMap["amount"] as Int
            println("초기 포인트: $initialAmount")
            
            // 2. 포인트 충전 (500)
            val chargeAmount = 500
            val chargeRequest = UserPointRequest.Charge(amount = chargeAmount.toLong())
            val chargeResponse = mockMvc.perform(
                post("/api/v1/user-points/{userId}/charge", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.amount").value(initialAmount + chargeAmount))
            .andReturn()
            
            val chargeResponseContent = chargeResponse.response.contentAsString
            val chargeResponseObj = objectMapper.readValue(chargeResponseContent, BaseResponse::class.java)
            val chargeDataMap = chargeResponseObj.data as Map<*, *>
            val afterFirstChargeAmount = chargeDataMap["amount"] as Int
            println("첫 번째 충전 후 포인트: $afterFirstChargeAmount")
            
            // 3. 추가 포인트 충전 (700)
            val additionalChargeAmount = 700
            val additionalChargeRequest = UserPointRequest.Charge(amount = additionalChargeAmount.toLong())
            val additionalChargeResponse = mockMvc.perform(
                post("/api/v1/user-points/{userId}/charge", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(additionalChargeRequest))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.amount").value(afterFirstChargeAmount + additionalChargeAmount))
            .andReturn()
            
            val additionalChargeResponseContent = additionalChargeResponse.response.contentAsString
            val additionalChargeResponseObj = objectMapper.readValue(additionalChargeResponseContent, BaseResponse::class.java)
            val additionalChargeDataMap = additionalChargeResponseObj.data as Map<*, *>
            val afterSecondChargeAmount = additionalChargeDataMap["amount"] as Int
            println("두 번째 충전 후 포인트: $afterSecondChargeAmount")
            
            // 4. 최종 포인트 확인
            val finalPointResponse = mockMvc.perform(
                get("/api/v1/user-points/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.amount").value(afterSecondChargeAmount))
            .andReturn()
            
            // 최종 포인트 값 확인
            val finalResponseContent = finalPointResponse.response.contentAsString
            val finalResponseObj = objectMapper.readValue(finalResponseContent, BaseResponse::class.java)
            val finalDataMap = finalResponseObj.data as Map<*, *>
            val finalAmount = finalDataMap["amount"] as Int
            
            // 전체 증가 포인트 검증
            val totalChargeAmount = chargeAmount + additionalChargeAmount
            org.junit.jupiter.api.Assertions.assertEquals(initialAmount + totalChargeAmount, finalAmount, 
                "초기 포인트($initialAmount) + 총 충전액($totalChargeAmount)이 최종 포인트($finalAmount)와 같아야 합니다")
            
            // 콘솔에 검증 결과 출력
            println("E2E 테스트 완료")
            println("초기 포인트: $initialAmount")
            println("충전액: $chargeAmount + $additionalChargeAmount = $totalChargeAmount")
            println("최종 포인트: $finalAmount")
        }
    }
} 