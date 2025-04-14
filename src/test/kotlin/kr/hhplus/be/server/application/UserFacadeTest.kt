package kr.hhplus.be.server.application

import kr.hhplus.be.server.application.user.UserCriteria
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.userpoint.UserPoint
import kr.hhplus.be.server.domain.userpoint.UserPointService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

class UserFacadeTest {

    private lateinit var userService: UserService
    private lateinit var userPointService: UserPointService
    private lateinit var userFacade: UserFacade

    @BeforeEach
    fun setUp() {
        userService = mock()
        userPointService = mock()
        userFacade = UserFacade(userService, userPointService)
    }

    @Test
    fun `사용자 생성 시 포인트도 함께 생성된다`() {
        // given
        val userId = "test-user-id"
        val mockUser = User(userId = userId)
        val mockUserPoint = UserPoint(
            userPointId = "test-point-id",
            userId = userId,
            amount = 0
        )

        `when`(userService.createUser()).thenReturn(mockUser)
        `when`(userPointService.create(any())).thenReturn(mockUserPoint)

        // when
        val result = userFacade.createUser()

        // then
        verify(userService).createUser()
        verify(userPointService).create(any())
        
        assertEquals(userId, result.userId)
        assertEquals(mockUser.createdAt, result.createdAt)
        assertEquals(mockUser.updatedAt, result.updatedAt)
    }
    
    @Test
    fun `ID로 사용자 조회 시 존재하는 경우 사용자 정보를 반환한다`() {
        // given
        val userId = "test-user-id"
        val mockUser = User(userId = userId)
        val criteria = UserCriteria.GetById(userId)
        
        `when`(userService.findUserByIdOrThrow(userId)).thenReturn(mockUser)
        
        // when
        val result = userFacade.findUserById(criteria)
        
        // then
        verify(userService).findUserByIdOrThrow(userId)
        assertEquals(userId, result.userId)
        assertEquals(mockUser.createdAt, result.createdAt)
        assertEquals(mockUser.updatedAt, result.updatedAt)
    }
    
    @Test
    fun `ID로 사용자 조회 시 존재하지 않는 경우 예외가 발생한다`() {
        // given
        val userId = "non-existent-user-id"
        val criteria = UserCriteria.GetById(userId)
        
        `when`(userService.findUserByIdOrThrow(userId)).thenThrow(UserException.NotFound("userId($userId)로 User를 찾을 수 없습니다."))
        
        // when & then
        assertThrows(UserException.NotFound::class.java) {
            userFacade.findUserById(criteria)
        }
        
        verify(userService).findUserByIdOrThrow(userId)
    }
    
    @Test
    fun `모든 사용자 조회 시 사용자 목록을 반환한다`() {
        // given
        val mockUsers = listOf(
            User(userId = "user-1"),
            User(userId = "user-2")
        )
        
        `when`(userService.getAllUsers()).thenReturn(mockUsers)
        
        // when
        val result = userFacade.getAllUsers()
        
        // then
        verify(userService).getAllUsers()
        assertEquals(2, result.users.size)
        assertEquals("user-1", result.users[0].userId)
        assertEquals("user-2", result.users[1].userId)
    }
} 