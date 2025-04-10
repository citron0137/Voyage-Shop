package kr.hhplus.be.server.domain.userpoint

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

class UserPointServiceUnitTest {

    private lateinit var userPointRepository: UserPointRepository
    private lateinit var userPointService: UserPointService

    @BeforeEach
    fun setUp() {
        userPointRepository = mock()
        userPointService = UserPointService(userPointRepository)
    }

    @Test
    fun `포인트 생성 테스트`() {
        // given
        val userId = "test-user-id"
        val command = UserPointCommand.Create(userId)
        val expectedUserPoint = UserPoint(
            userPointId = "test-point-id",
            userId = userId,
            amount = 0
        )
        
        `when`(userPointRepository.create(any())).thenReturn(expectedUserPoint)

        // when
        val result = userPointService.create(command)

        // then
        assertEquals(expectedUserPoint.userId, result.userId)
        assertEquals(0, result.amount)
        verify(userPointRepository).create(any())
    }

    @Test
    fun `포인트 조회 테스트 - 존재하는 경우`() {
        // given
        val userId = "test-user-id"
        val expectedUserPoint = UserPoint(
            userPointId = "test-point-id",
            userId = userId,
            amount = 1000
        )
        
        `when`(userPointRepository.findByUserId(userId)).thenReturn(expectedUserPoint)

        // when
        val result = userPointService.findByUserId(userId)

        // then
        assertNotNull(result)
        assertEquals(expectedUserPoint, result)
        verify(userPointRepository).findByUserId(userId)
    }

    @Test
    fun `포인트 조회 테스트 - 존재하지 않는 경우`() {
        // given
        val userId = "non-existent-user-id"
        `when`(userPointRepository.findByUserId(userId)).thenReturn(null)

        // when
        val result = userPointService.findByUserId(userId)

        // then
        assertNull(result)
        verify(userPointRepository).findByUserId(userId)
    }

    @Test
    fun `포인트 충전 테스트 - 정상 케이스`() {
        // given
        val userId = "test-user-id"
        val initialAmount = 1000L
        val chargeAmount = 500L
        val command = UserPointCommand.Charge(userId, chargeAmount)
        
        val existingUserPoint = UserPoint(
            userPointId = "test-point-id",
            userId = userId,
            amount = initialAmount
        )
        
        `when`(userPointRepository.findByUserId(userId)).thenReturn(existingUserPoint)
        `when`(userPointRepository.save(any())).thenAnswer { it.arguments[0] }

        // when
        val result = userPointService.charge(command)

        // then
        assertEquals(initialAmount + chargeAmount, result.amount)
        verify(userPointRepository).findByUserId(userId)
        verify(userPointRepository).save(any())
    }

    @Test
    fun `포인트 충전 테스트 - 0원 이하 충전 시도`() {
        // given
        val userId = "test-user-id"
        val command = UserPointCommand.Charge(userId, 0)

        // when & then
        assertThrows<UserPointException.ChargeAmountShouldMoreThan0> {
            userPointService.charge(command)
        }
    }

    @Test
    fun `포인트 충전 테스트 - 최대치 초과`() {
        // given
        val userId = "test-user-id"
        val command = UserPointCommand.Charge(userId, Long.MAX_VALUE)
        
        val existingUserPoint = UserPoint(
            userPointId = "test-point-id",
            userId = userId,
            amount = 1
        )
        
        `when`(userPointRepository.findByUserId(userId)).thenReturn(existingUserPoint)

        // when & then
        assertThrows<UserPointException.PointAmountOverflow> {
            userPointService.charge(command)
        }
    }

    @Test
    fun `포인트 사용 테스트 - 정상 케이스`() {
        // given
        val userId = "test-user-id"
        val initialAmount = 1000L
        val useAmount = 500L
        val command = UserPointCommand.Use(userId, useAmount)
        
        val existingUserPoint = UserPoint(
            userPointId = "test-point-id",
            userId = userId,
            amount = initialAmount
        )
        
        `when`(userPointRepository.findByUserId(userId)).thenReturn(existingUserPoint)
        `when`(userPointRepository.save(any())).thenAnswer { it.arguments[0] }

        // when
        val result = userPointService.use(command)

        // then
        assertEquals(initialAmount - useAmount, result.amount)
        verify(userPointRepository).findByUserId(userId)
        verify(userPointRepository).save(any())
    }

    @Test
    fun `포인트 사용 테스트 - 0원 이하 사용 시도`() {
        // given
        val userId = "test-user-id"
        val command = UserPointCommand.Use(userId, 0)

        // when & then
        assertThrows<UserPointException.UseAmountShouldMoreThan0> {
            userPointService.use(command)
        }
    }

    @Test
    fun `포인트 사용 테스트 - 잔액 부족`() {
        // given
        val userId = "test-user-id"
        val command = UserPointCommand.Use(userId, 1000L)
        
        val existingUserPoint = UserPoint(
            userPointId = "test-point-id",
            userId = userId,
            amount = 500L
        )
        
        `when`(userPointRepository.findByUserId(userId)).thenReturn(existingUserPoint)

        // when & then
        assertThrows<UserPointException.PointAmountUnderflow> {
            userPointService.use(command)
        }
    }
}
