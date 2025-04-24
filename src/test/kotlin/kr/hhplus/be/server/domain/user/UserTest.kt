package kr.hhplus.be.server.domain.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("사용자 엔티티 테스트")
class UserTest {

    @Nested
    @DisplayName("사용자 생성 시")
    inner class UserCreation {
        
        @Test
        @DisplayName("기본 생성자로 생성하면 현재 시간이 설정되어야 한다")
        fun withDefaultConstructor_setsCurrentTime() {
            // given
            val userId = "test-user-id"
            
            // when
            val user = User(userId)
            
            // then
            assertEquals(userId, user.userId)
            assertNotNull(user.createdAt)
            assertNotNull(user.updatedAt)
        }
        
        @Test
        @DisplayName("시간을 지정하여 생성하면 지정된 시간이 설정되어야 한다")
        fun withSpecificTime_setsSpecifiedTime() {
            // given
            val userId = "test-user-id"
            val createdAt = LocalDateTime.of(2023, 1, 1, 0, 0)
            val updatedAt = LocalDateTime.of(2023, 1, 2, 0, 0)
            
            // when
            val user = User(userId, createdAt, updatedAt)
            
            // then
            assertEquals(userId, user.userId)
            assertEquals(createdAt, user.createdAt)
            assertEquals(updatedAt, user.updatedAt)
        }
    }
    
    @Nested
    @DisplayName("사용자 데이터 비교 시")
    inner class UserComparison {
        
        @Test
        @DisplayName("동일한 userId를 가진 사용자는 동등해야 한다")
        fun withSameUserId_equals() {
            // given
            val userId = "test-user-id"
            val createdAt = LocalDateTime.of(2023, 1, 1, 0, 0)
            val updatedAt = LocalDateTime.of(2023, 1, 2, 0, 0)
            
            // when
            val user1 = User(userId, createdAt, updatedAt)
            val user2 = User(userId, createdAt, updatedAt)
            
            // then
            assertEquals(user1, user2)
        }
    }
} 