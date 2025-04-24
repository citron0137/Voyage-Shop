package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.userpoint.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        val criteria = UserPointCriteria.GetByUserId(userId)
        val user = User(userId = userId)
        val userPoint = UserPoint(
            userPointId = "point1",
            userId = userId,
            amount = 1000
        )
        
        whenever(userService.getUserById(any())).thenReturn(user)
        whenever(userPointService.getByUserId(UserPointQuery.GetByUserId(userId))).thenReturn(userPoint)
        
        // when
        val result = userPointFacade.getUserPoint(criteria)
        
        // then
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.amount).isEqualTo(1000)
    }
    
    @Test
    @DisplayName("포인트가 없는 사용자의 포인트를 조회하면 예외가 발생한다")
    fun getUserPointWithNoPoint() {
        // given
        val userId = "user1"
        val criteria = UserPointCriteria.GetByUserId(userId)
        val user = User(userId = userId)
        
        whenever(userService.getUserById(any())).thenReturn(user)
        whenever(userPointService.getByUserId(UserPointQuery.GetByUserId(userId)))
            .thenThrow(UserPointException.NotFound("userId(${userId})로 UserPoint를 찾을 수 없습니다."))
        
        // when, then
        assertThrows<UserPointException.NotFound> {
            userPointFacade.getUserPoint(criteria)
        }
    }
    
    @Test
    @DisplayName("사용자 포인트를 충전한다")
    fun chargePoint() {
        // given
        val userId = "user1"
        val amount = 1000L
        val criteria = UserPointCriteria.Charge(userId, amount)
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
        
        whenever(userService.getUserById(any())).thenReturn(user)
        whenever(userPointService.charge(any<UserPointCommand.Charge>())).thenReturn(userPointAfter)
        
        // when
        val result = userPointFacade.chargePoint(criteria)
        
        // then
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.amount).isEqualTo(1500)
    }
}