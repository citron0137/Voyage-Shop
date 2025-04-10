package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@DisplayName("CouponUserFacade 테스트")
class CouponUserFacadeTest {
    
    private lateinit var couponUserService: CouponUserService
    private lateinit var userService: UserService
    private lateinit var couponUserFacade: CouponUserFacade
    
    @BeforeEach
    fun setup() {
        couponUserService = mock()
        userService = mock()
        couponUserFacade = CouponUserFacade(couponUserService, userService)
    }
    
    @Test
    @DisplayName("유저의 모든 쿠폰을 조회한다")
    fun getAllCouponsByUserId() {
        // given
        val userId = "user1"
        val user = User(userId = userId)
        val couponUsers = listOf(
            CouponUser(
                couponUserId = "coupon1",
                userId = userId,
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = "coupon2",
                userId = userId,
                benefitMethod = CouponBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        whenever(userService.findUserByIdOrThrow(userId)).thenReturn(user)
        whenever(couponUserService.getCouponUsersByUserId(userId)).thenReturn(couponUsers)
        
        // when
        val result = couponUserFacade.getAllCouponsByUserId(userId)
        
        // then
        assertThat(result.couponUsers).hasSize(2)
        assertThat(result.couponUsers[0].couponUserId).isEqualTo("coupon1")
        assertThat(result.couponUsers[0].benefitMethod).isEqualTo(CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT)
        assertThat(result.couponUsers[0].benefitAmount).isEqualTo("1000")
        assertThat(result.couponUsers[1].couponUserId).isEqualTo("coupon2")
        assertThat(result.couponUsers[1].benefitMethod).isEqualTo(CouponBenefitMethod.DISCOUNT_PERCENTAGE)
        assertThat(result.couponUsers[1].benefitAmount).isEqualTo("10")
    }
    
    @Test
    @DisplayName("빈 유저 ID로 쿠폰을 조회하면 예외가 발생한다")
    fun getAllCouponsByUserIdWithBlankUserId() {
        // given
        val userId = ""
        
        // when, then
        assertThrows<UserException.UserIdShouldNotBlank> {
            couponUserFacade.getAllCouponsByUserId(userId)
        }
    }
    
    @Test
    @DisplayName("존재하지 않는 유저의 쿠폰을 조회하면 예외가 발생한다")
    fun getAllCouponsByUserIdWithNonExistingUser() {
        // given
        val userId = "non-existing-user"
        
        whenever(userService.findUserByIdOrThrow(userId)).thenThrow(UserException.NotFound("유저를 찾을 수 없습니다"))
        
        // when, then
        assertThrows<UserException.NotFound> {
            couponUserFacade.getAllCouponsByUserId(userId)
        }
    }
    
    @Test
    @DisplayName("쿠폰 ID로 쿠폰을 조회한다")
    fun getCouponUser() {
        // given
        val couponUserId = "coupon1"
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user1",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        whenever(couponUserService.getCouponUser(couponUserId)).thenReturn(couponUser)
        
        // when
        val result = couponUserFacade.getCouponUser(couponUserId)
        
        // then
        assertThat(result.couponUserId).isEqualTo(couponUserId)
        assertThat(result.benefitMethod).isEqualTo(CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT)
        assertThat(result.benefitAmount).isEqualTo("1000")
    }
    
    @Test
    @DisplayName("빈 쿠폰 ID로 조회하면 예외가 발생한다")
    fun getCouponUserWithBlankId() {
        // given
        val couponUserId = ""
        
        // when, then
        assertThrows<CouponException.CouponUserIdShouldNotBlank> {
            couponUserFacade.getCouponUser(couponUserId)
        }
    }
    
    @Test
    @DisplayName("존재하지 않는 쿠폰을 조회하면 예외가 발생한다")
    fun getCouponUserWithNonExistingId() {
        // given
        val couponUserId = "non-existing-coupon"
        
        whenever(couponUserService.getCouponUser(couponUserId)).thenThrow(CouponException.NotFound("쿠폰을 찾을 수 없습니다"))
        
        // when, then
        assertThrows<CouponException.NotFound> {
            couponUserFacade.getCouponUser(couponUserId)
        }
    }
    
    @Test
    @DisplayName("쿠폰을 발급한다")
    fun issueCoupon() {
        // given
        val userId = "user1"
        val benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT
        val benefitAmount = "1000"
        val user = User(userId = userId)
        val createdCouponUser = CouponUser(
            couponUserId = "new-coupon-id",
            userId = userId,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        whenever(userService.findUserByIdOrThrow(userId)).thenReturn(user)
        whenever(couponUserService.create(any())).thenReturn(createdCouponUser)
        
        // when
        val result = couponUserFacade.issueCoupon(userId, benefitMethod, benefitAmount)
        
        // then
        assertThat(result.couponUserId).isEqualTo("new-coupon-id")
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.benefitMethod).isEqualTo(benefitMethod)
        assertThat(result.benefitAmount).isEqualTo(benefitAmount)
    }
    
    @Test
    @DisplayName("쿠폰을 사용한다")
    fun useCoupon() {
        // given
        val couponUserId = "coupon1"
        val couponUserBefore = CouponUser(
            couponUserId = couponUserId,
            userId = "user1",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val now = LocalDateTime.now()
        val couponUserAfter = CouponUser(
            couponUserId = couponUserId,
            userId = "user1",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = now,
            createdAt = couponUserBefore.createdAt,
            updatedAt = now
        )
        
        whenever(couponUserService.use(any())).thenReturn(couponUserAfter)
        
        // when
        val result = couponUserFacade.useCoupon(couponUserId)
        
        // then
        assertThat(result.couponUserId).isEqualTo(couponUserId)
        assertThat(result.usedAt).isNotNull()
    }
    
    @Test
    @DisplayName("이미 사용된 쿠폰을 사용하면 예외가 발생한다")
    fun useCouponWithAlreadyUsedCoupon() {
        // given
        val couponUserId = "coupon1"
        
        whenever(couponUserService.use(any())).thenThrow(CouponException.AlreadyUsed("이미 사용된 쿠폰입니다"))
        
        // when, then
        assertThrows<CouponException.AlreadyUsed> {
            couponUserFacade.useCoupon(couponUserId)
        }
    }
    
    @Test
    @DisplayName("고정 금액 할인 쿠폰으로 할인 금액을 계산한다")
    fun calculateDiscountAmountWithFixedAmount() {
        // given
        val couponUserId = "coupon1"
        val originalAmount = 10000L
        val discountAmount = 1000L
        val couponUser = mock<CouponUser>()
        
        whenever(couponUserService.getCouponUser(couponUserId)).thenReturn(couponUser)
        whenever(couponUser.calculateDiscountAmount(originalAmount)).thenReturn(discountAmount)
        
        // when
        val result = couponUserFacade.calculateDiscountAmount(couponUserId, originalAmount)
        
        // then
        assertThat(result).isEqualTo(discountAmount)
    }
    
    @Test
    @DisplayName("퍼센트 할인 쿠폰으로 할인 금액을 계산한다")
    fun calculateDiscountAmountWithPercentage() {
        // given
        val couponUserId = "coupon1"
        val originalAmount = 10000L
        val discountPercentage = 10L
        val expectedDiscountAmount = 1000L // 10% of 10000
        val couponUser = mock<CouponUser>()
        
        whenever(couponUserService.getCouponUser(couponUserId)).thenReturn(couponUser)
        whenever(couponUser.calculateDiscountAmount(originalAmount)).thenReturn(expectedDiscountAmount)
        
        // when
        val result = couponUserFacade.calculateDiscountAmount(couponUserId, originalAmount)
        
        // then
        assertThat(result).isEqualTo(expectedDiscountAmount)
    }
    
    @Test
    @DisplayName("모든 쿠폰을 조회한다")
    fun getAllCoupons() {
        // given
        val couponUsers = listOf(
            CouponUser(
                couponUserId = "coupon1",
                userId = "user1",
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = "coupon2",
                userId = "user2",
                benefitMethod = CouponBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = "coupon3",
                userId = "user1",
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "2000",
                usedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        whenever(couponUserService.getAllCouponUsers()).thenReturn(couponUsers)
        
        // when
        val result = couponUserFacade.getAllCoupons()
        
        // then
        assertThat(result.couponUsers).hasSize(3)
        assertThat(result.couponUsers[0].couponUserId).isEqualTo("coupon1")
        assertThat(result.couponUsers[1].couponUserId).isEqualTo("coupon2")
        assertThat(result.couponUsers[2].couponUserId).isEqualTo("coupon3")
        
        // 다양한 상태의 쿠폰이 잘 조회되는지 확인
        assertThat(result.couponUsers.filter { it.userId == "user1" }).hasSize(2)
        assertThat(result.couponUsers.filter { it.userId == "user2" }).hasSize(1)
        assertThat(result.couponUsers.filter { it.usedAt != null }).hasSize(1)
        assertThat(result.couponUsers.filter { it.benefitMethod == CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT }).hasSize(2)
        assertThat(result.couponUsers.filter { it.benefitMethod == CouponBenefitMethod.DISCOUNT_PERCENTAGE }).hasSize(1)
    }
} 