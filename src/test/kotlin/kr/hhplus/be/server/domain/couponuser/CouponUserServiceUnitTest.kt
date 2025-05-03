package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.couponuser.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("CouponUserService 단위 테스트")
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
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000"
        )
        val expectedCouponUser = CouponUser(
            couponUserId = UUID.randomUUID().toString(),
            userId = command.userId,
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = command.benefitAmount,
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.create(argThat { this.userId == command.userId })).thenReturn(expectedCouponUser)

        // when
        val result = couponUserService.create(command)

        // then
        assertEquals(expectedCouponUser.couponUserId, result.couponUserId)
        assertEquals(command.userId, result.userId)
        assertEquals(CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT, result.benefitMethod)
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
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)
        `when`(couponUserRepository.update(argThat { this.couponUserId == couponUserId && this.usedAt != null }))
            .thenReturn(couponUser.use())

        // when
        val result = couponUserService.useCoupon(command)

        // then
        assertEquals(couponUserId, result.couponUserId)
        assertEquals("user-id", result.userId)
        assertEquals(CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT, result.benefitMethod)
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
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)

        // when & then
        assertThrows<CouponUserException.AlreadyUsed> {
            couponUserService.useCoupon(command)
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
        assertThrows<CouponUserException.NotFound> {
            couponUserService.useCoupon(command)
        }
    }

    @Test
    @DisplayName("사용자의 쿠폰 목록을 조회할 수 있다")
    fun `사용자의 쿠폰 목록을 조회할 수 있다`() {
        // given
        val userId = "user-id"
        val command = CouponUserCommand.GetByUserId(userId)
        val couponUsers = listOf(
            CouponUser(
                couponUserId = UUID.randomUUID().toString(),
                userId = userId,
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = UUID.randomUUID().toString(),
                userId = userId,
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        `when`(couponUserRepository.findByUserId(userId)).thenReturn(couponUsers)

        // when
        val result = couponUserService.getAllCouponsByUserId(command)

        // then
        assertEquals(2, result.size)
        assertEquals(couponUsers[0].couponUserId, result[0].couponUserId)
        assertEquals(couponUsers[1].couponUserId, result[1].couponUserId)
    }

    @Test
    @DisplayName("쿠폰 ID로 쿠폰을 조회한다")
    fun `쿠폰 ID로 쿠폰을 조회한다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)

        // when
        val command = CouponUserCommand.GetById(couponUserId)
        val result = couponUserService.getCouponUser(command)

        // then
        assertEquals(couponUserId, result.couponUserId)
        assertEquals("user-id", result.userId)
        assertEquals(CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT, result.benefitMethod)
        assertEquals("1000", result.benefitAmount)
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 ID로 조회하면 예외가 발생한다")
    fun `존재하지 않는 쿠폰 ID로 조회하면 예외가 발생한다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(null)

        // when & then
        val command = CouponUserCommand.GetById(couponUserId)
        assertThrows<CouponUserException.NotFound> {
            couponUserService.getCouponUser(command)
        }
    }
    
    @Test
    @DisplayName("모든 쿠폰 사용자 정보를 조회할 수 있다")
    fun `모든 쿠폰 사용자 정보를 조회할 수 있다`() {
        // given
        val command = CouponUserCommand.GetAll()
        val couponUsers = listOf(
            CouponUser(
                couponUserId = UUID.randomUUID().toString(),
                userId = "user-id-1",
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = UUID.randomUUID().toString(),
                userId = "user-id-2",
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        `when`(couponUserRepository.findAll()).thenReturn(couponUsers)

        // when
        val result = couponUserService.getAllCouponUsers(command)

        // then
        assertEquals(2, result.size)
        assertEquals(couponUsers[0].couponUserId, result[0].couponUserId)
        assertEquals(couponUsers[1].couponUserId, result[1].couponUserId)
    }
    
    @Test
    @DisplayName("쿠폰으로 할인 금액을 계산할 수 있다 - 고정 금액 할인")
    fun `쿠폰으로 할인 금액을 계산할 수 있다 - 고정 금액 할인`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val originalAmount = 10000L
        val command = CouponUserCommand.CalculateDiscount(
            couponUserId = couponUserId,
            originalAmount = originalAmount
        )
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)

        // when
        val discountAmount = couponUserService.calculateDiscountAmount(command)

        // then
        assertEquals(1000L, discountAmount)
    }
    
    @Test
    @DisplayName("쿠폰으로 할인 금액을 계산할 수 있다 - 퍼센트 할인")
    fun `쿠폰으로 할인 금액을 계산할 수 있다 - 퍼센트 할인`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val originalAmount = 10000L
        val command = CouponUserCommand.CalculateDiscount(
            couponUserId = couponUserId,
            originalAmount = originalAmount
        )
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
            benefitAmount = "10",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(couponUser)

        // when
        val discountAmount = couponUserService.calculateDiscountAmount(command)

        // then
        assertEquals(1000L, discountAmount) // 10000의 10%인 1000
    }
    
    @Test
    @DisplayName("존재하지 않는 쿠폰으로 할인 계산하면 예외가 발생한다")
    fun `존재하지 않는 쿠폰으로 할인 계산하면 예외가 발생한다`() {
        // given
        val couponUserId = UUID.randomUUID().toString()
        val originalAmount = 10000L
        val command = CouponUserCommand.CalculateDiscount(
            couponUserId = couponUserId,
            originalAmount = originalAmount
        )
        `when`(couponUserRepository.findById(couponUserId)).thenReturn(null)

        // when & then
        assertThrows<CouponUserException.NotFound> {
            couponUserService.calculateDiscountAmount(command)
        }
    }
} 