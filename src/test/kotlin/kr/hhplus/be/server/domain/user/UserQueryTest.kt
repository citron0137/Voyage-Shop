package kr.hhplus.be.server.domain.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("사용자 쿼리 테스트")
class UserQueryTest {

    @Nested
    @DisplayName("ID로 사용자 조회 쿼리 생성 시")
    inner class GetByIdQueryCreation {
        
        @Test
        @DisplayName("유효한 ID로 생성하면 성공해야 한다")
        fun withValidId_succeeds() {
            // given
            val userId = "test-user-id"
            
            // when
            val query = UserQuery.GetById(userId)
            
            // then
            assertEquals(userId, query.userId)
        }
        
        @Test
        @DisplayName("빈 ID로 생성하면 예외가 발생해야 한다")
        fun withBlankId_throwsException() {
            // given
            val userId = ""
            
            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                UserQuery.GetById(userId)
            }
            
            assertEquals("userId must not be blank", exception.message)
        }
        
        @Test
        @DisplayName("공백 ID로 생성하면 예외가 발생해야 한다")
        fun withWhitespaceId_throwsException() {
            // given
            val userId = "   "
            
            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                UserQuery.GetById(userId)
            }
            
            assertEquals("userId must not be blank", exception.message)
        }
    }
    
    @Nested
    @DisplayName("모든 사용자 조회 쿼리 생성 시")
    inner class GetAllQueryCreation {
        
        @Test
        @DisplayName("객체 생성이 성공해야 한다")
        fun creation_succeeds() {
            // when
            val query = UserQuery.GetAll
            
            // then
            assertEquals(UserQuery.GetAll, query)
        }
    }
} 