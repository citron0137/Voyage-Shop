package kr.hhplus.be.server.controllers.integration

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.controller.user.UserResponse
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

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@DisplayName("사용자 API 테스트")
@Transactional
class UserApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
    }
    
    @Nested
    @DisplayName("사용자 생성 시")
    inner class UserCreation {
        
        @Test
        @DisplayName("요청하면 새 사용자가 생성되어야 한다")
        fun request_createsNewUser() {
            // when
            val result = mockMvc.perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists())
        }
    }
    
    @Nested
    @DisplayName("사용자 조회 시")
    inner class UserRetrieval {
        
        @Test
        @DisplayName("존재하는 ID로 조회하면 사용자 정보가 반환되어야 한다")
        fun withExistingId_returnsUser() {
            // given: 사용자 생성
            val createResponse = mockMvc.perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andReturn()
            
            val responseContent = createResponse.response.contentAsString
            val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
            val dataMap = responseObj.data as Map<*, *>
            val userId = dataMap["id"] as String
            
            // when: 생성된 사용자 조회
            val result = mockMvc.perform(
                get("/users/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
        }
        
        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 에러가 반환되어야 한다")
        fun withNonExistentId_returnsError() {
            // when
            val result = mockMvc.perform(
                get("/users/{userId}", "non-existent-id")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk) // API는 항상 200을 반환하고 내부 코드로 에러 처리
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
        }
    }
    
    @Nested
    @DisplayName("사용자 목록 조회 시")
    inner class UserListRetrieval {
        
        @Test
        @DisplayName("모든 사용자 목록이 반환되어야 한다")
        fun request_returnsAllUsers() {
            // given: 사용자 여러 명 생성
            for (i in 1..3) {
                mockMvc.perform(
                    post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                )
            }
            
            // when: 사용자 목록 조회
            val result = mockMvc.perform(
                get("/users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            
            // then
            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray)
                .andExpect(jsonPath("$.data.items.length()").value(3))
        }
    }
    
    @Nested
    @DisplayName("E2E 테스트 시나리오")
    inner class E2EScenario {
        
        @Test
        @DisplayName("사용자 생성 및 조회 흐름이 정상 동작해야 한다")
        fun createAndRetrieveUser_succeedsEndToEnd() {
            // 1. 사용자 생성
            val createResponse = mockMvc.perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
            
            val responseContent = createResponse.response.contentAsString
            val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
            val dataMap = responseObj.data as Map<*, *>
            val userId = dataMap["id"] as String
            
            // 2. 생성된 사용자 조회
            mockMvc.perform(
                get("/users/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
            
            // 3. 사용자 목록 조회
            mockMvc.perform(
                get("/users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray)
                .andExpect(jsonPath("$.data.items[0].id").exists())
        }
    }
} 