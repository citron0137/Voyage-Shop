package kr.hhplus.be.server.domain.coupon

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CouponUserServiceUnitTest {
    @Mock
    private lateinit var couponUserRepository: CouponUserRepository

    @InjectMocks
    private lateinit var couponUserService: CouponUserService

    @Test
    @DisplayName("쿠폰을 생성할 수 있다")
    fun `쿠폰을 생성할 수 있다`() {
        // given
        val command = CouponUserCommand.Create(
            userId = "user-id",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000"
        )
        val expectedCouponUser = CouponUser(
            couponUserId = UUID.randomUUID().toString(),
            userId = command.userId,
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = command.benefitAmount,
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.create(any<CouponUser>())).thenReturn(expectedCouponUser)

        // when
        val result = couponUserService.create(command)

        // then
        assertEquals(expectedCouponUser.couponUserId, result.couponUserId)
        assertEquals(command.userId, result.userId)
        assertEquals(CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT, result.benefitMethod)
        assertEquals(command.benefitAmount, result.benefitAmount)
        assertEquals(null, result.usedAt)
    }

    @Test
    @DisplayName("쿠폰을 사용할 수 있다")
    fun `쿠폰을 사용할 수 있다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val command = CouponUserCommand.Use(couponUserId = couponUserId)
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user-id",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)
        `when`(couponUserRepository.update(any<CouponUser>())).thenReturn(couponUser)

        // when
        val result = couponUserService.use(command)

        // then
        assertEquals(couponUserId, result.couponUserId)
        assertEquals("user-id", result.userId)
        assertEquals(CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT, result.benefitMethod)
        assertEquals("1000", result.benefitAmount)
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 사용하면 예외가 발생한다")
    fun `이미 사용된 쿠폰을 사용하면 예외가 발생한다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val command = CouponUserCommand.Use(couponUserId = couponUserId)
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user-id",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)

        // when & then
        assertThrows<CouponException.AlreadyUsed> {
            couponUserService.use(command)
        }
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰을 사용하면 예외가 발생한다")
    fun `존재하지 않는 쿠폰을 사용하면 예외가 발생한다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val command = CouponUserCommand.Use(couponUserId = couponUserId)
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(null)

        // when & then
        assertThrows<CouponException.NotFound> {
            couponUserService.use(command)
        }
    }

    @Test
    @DisplayName("사용자의 쿠폰 목록을 조회할 수 있다")
    fun `사용자의 쿠폰 목록을 조회할 수 있다`() {
        // given
        val userId = "user-id"
        val couponUsers = listOf(
            CouponUser(
                couponUserId = UUID.randomUUID().toString(),
                userId = userId,
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = UUID.randomUUID().toString(),
                userId = userId,
                benefitMethod = CouponBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        `when`(couponUserRepository.findByUserId(userId)).thenReturn(couponUsers)

        // when
        val result = couponUserService.getCouponUsersByUserId(userId)

        // then
        assertEquals(2, result.size)
        assertEquals(couponUsers[0].couponUserId, result[0].couponUserId)
        assertEquals(couponUsers[1].couponUserId, result[1].couponUserId)
    }

    @Test
    @DisplayName("쿠폰 ID로 쿠폰을 조회할 수 있다")
    fun `쿠폰 ID로 쿠폰을 조회할 수 있다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user-id",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)

        // when
        val result = couponUserService.getCouponUser(couponUserId)

        // then
        assertEquals(couponUserId, result.couponUserId)
        assertEquals("user-id", result.userId)
        assertEquals(CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT, result.benefitMethod)
        assertEquals("1000", result.benefitAmount)
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 ID로 조회하면 예외가 발생한다")
    fun `존재하지 않는 쿠폰 ID로 조회하면 예외가 발생한다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(null)

        // when & then
        assertThrows<CouponException.NotFound> {
            couponUserService.getCouponUser(couponUserId)
        }
    }
} 