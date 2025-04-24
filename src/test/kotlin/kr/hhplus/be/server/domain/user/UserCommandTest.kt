package kr.hhplus.be.server.domain.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("사용자 명령 테스트")
class UserCommandTest {

    @Nested
    @DisplayName("사용자 생성 명령 생성 시")
    inner class CreateCommandCreation {
        
        @Test
        @DisplayName("객체 생성이 성공해야 한다")
        fun creation_succeeds() {
            // when
            val command = UserCommand.Create
            
            // then
            assertEquals(UserCommand.Create, command)
        }
    }
} 