package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.userpoint.UserPoint
import kr.hhplus.be.server.domain.userpoint.UserPointCommand
import kr.hhplus.be.server.domain.userpoint.UserPointException
import kr.hhplus.be.server.domain.userpoint.UserPointService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@DisplayName("UserPointFacade 테스트")
class UserPointFacadeTest {
    
    private lateinit var userService: UserService
    private lateinit var userPointService: UserPointService
    private lateinit var userPointFacade: UserPointFacade
    
    @BeforeEach
    fun setup() {
        userService = mock()
        userPointService = mock()
        userPointFacade = UserPointFacade(userPointService, userService)
    }
    
    @Test
    @DisplayName("사용자 포인트를 조회한다")
    fun getUserPoint() {
        // given
        val userId = "user1"
        val user = User(userId = userId)
        val userPoint = UserPoint(
            userPointId = "point1",
            userId = userId,
            amount = 1000
        )
        
        whenever(userService.findUserByIdOrThrow(userId)).thenReturn(user)
        whenever(userPointService.findByUserId(userId)).thenReturn(userPoint)
        
        // when
        val result = userPointFacade.getUserPoint(userId)
        
        // then
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.amount).isEqualTo(1000)
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자의 포인트를 조회하면 예외가 발생한다")
    fun getUserPointWithNonExistingUser() {
        // given
        val userId = "non-existing-user"
        
        whenever(userService.findUserByIdOrThrow(userId)).thenThrow(UserException.NotFound("사용자를 찾을 수 없습니다"))
        
        // when, then
        assertThrows<UserException.NotFound> {
            userPointFacade.getUserPoint(userId)
        }
    }
    
    @Test
    @DisplayName("포인트가 없는 사용자의 포인트를 조회하면 예외가 발생한다")
    fun getUserPointWithNoPoint() {
        // given
        val userId = "user1"
        val user = User(userId = userId)
        
        whenever(userService.findUserByIdOrThrow(userId)).thenReturn(user)
        whenever(userPointService.findByUserId(userId)).thenReturn(null)
        
        // when, then
        assertThrows<UserPointException.NotFound> {
            userPointFacade.getUserPoint(userId)
        }
    }
    
    @Test
    @DisplayName("사용자 포인트를 충전한다")
    fun chargePoint() {
        // given
        val userId = "user1"
        val amount = 1000L
        val user = User(userId = userId)
        val userPointBefore = UserPoint(
            userPointId = "point1",
            userId = userId,
            amount = 500
        )
        val userPointAfter = UserPoint(
            userPointId = "point1",
            userId = userId,
            amount = 1500
        )
        
        whenever(userService.findUserByIdOrThrow(userId)).thenReturn(user)
        whenever(userPointService.charge(any<UserPointCommand.Charge>())).thenReturn(userPointAfter)
        
        // when
        val result = userPointFacade.chargePoint(userId, amount)
        
        // then
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.amount).isEqualTo(1500)
    }
}