package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@DisplayName("CouponUserFacade 테스트")
class CouponUserFacadeTest {
    
    @Mock
    private lateinit var couponUserService: CouponUserService
    
    @Mock
    private lateinit var userService: UserService
    
    @InjectMocks
    private lateinit var couponUserFacade: CouponUserFacade
    
    @Test
    @DisplayName("유저의 모든 쿠폰을 조회한다")
    fun getAllCouponsByUserId() {
        // given
        val userId = "user1"
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
        
        `when`(couponUserService.getAllCouponsByUserId(userId, userService)).thenReturn(couponUsers)
        
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
        
        verify(couponUserService, times(1)).getAllCouponsByUserId(userId, userService)
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
        
        `when`(couponUserService.getCouponUserWithValidation(couponUserId)).thenReturn(couponUser)
        
        // when
        val result = couponUserFacade.getCouponUser(couponUserId)
        
        // then
        assertThat(result.couponUserId).isEqualTo(couponUserId)
        assertThat(result.benefitMethod).isEqualTo(CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT)
        assertThat(result.benefitAmount).isEqualTo("1000")
        
        verify(couponUserService, times(1)).getCouponUserWithValidation(couponUserId)
    }
    
    @Test
    @DisplayName("쿠폰을 발급한다")
    fun issueCoupon() {
        // given
        val userId = "user1"
        val benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT
        val benefitAmount = "1000"
        val createdCouponUser = CouponUser(
            couponUserId = "new-coupon-id",
            userId = userId,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        `when`(couponUserService.issueCoupon(userId, benefitMethod, benefitAmount, userService)).thenReturn(createdCouponUser)
        
        // when
        val result = couponUserFacade.issueCoupon(userId, benefitMethod, benefitAmount)
        
        // then
        assertThat(result.couponUserId).isEqualTo("new-coupon-id")
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.benefitMethod).isEqualTo(benefitMethod)
        assertThat(result.benefitAmount).isEqualTo(benefitAmount)
        
        verify(couponUserService, times(1)).issueCoupon(userId, benefitMethod, benefitAmount, userService)
    }
    
    @Test
    @DisplayName("쿠폰을 사용한다")
    fun useCoupon() {
        // given
        val couponUserId = "coupon1"
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user1",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        `when`(couponUserService.useCoupon(couponUserId)).thenReturn(couponUser)
        
        // when
        val result = couponUserFacade.useCoupon(couponUserId)
        
        // then
        assertThat(result.couponUserId).isEqualTo(couponUserId)
        assertThat(result.usedAt).isNotNull()
        
        verify(couponUserService, times(1)).useCoupon(couponUserId)
    }
    
    @Test
    @DisplayName("쿠폰으로 할인 금액을 계산한다")
    fun calculateDiscountAmount() {
        // given
        val couponUserId = "coupon1"
        val originalAmount = 10000L
        val discountAmount = 1000L
        
        `when`(couponUserService.calculateDiscountAmount(couponUserId, originalAmount)).thenReturn(discountAmount)
        
        // when
        val result = couponUserFacade.calculateDiscountAmount(couponUserId, originalAmount)
        
        // then
        assertThat(result).isEqualTo(discountAmount)
        
        verify(couponUserService, times(1)).calculateDiscountAmount(couponUserId, originalAmount)
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
            )
        )
        
        `when`(couponUserService.getAllCouponUsers()).thenReturn(couponUsers)
        
        // when
        val result = couponUserFacade.getAllCoupons()
        
        // then
        assertThat(result.couponUsers).hasSize(2)
        assertThat(result.couponUsers[0].couponUserId).isEqualTo("coupon1")
        assertThat(result.couponUsers[1].couponUserId).isEqualTo("coupon2")
        
        verify(couponUserService, times(1)).getAllCouponUsers()
    }
} 