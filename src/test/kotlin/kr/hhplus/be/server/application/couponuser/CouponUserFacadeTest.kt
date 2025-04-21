package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.couponuser.CouponUser
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import kr.hhplus.be.server.domain.couponuser.CouponUserCommand
import kr.hhplus.be.server.domain.couponuser.CouponUserService
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
import org.mockito.kotlin.any
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
        val criteria = CouponUserCriteria.GetByUserId(userId)
        val serviceCommand = CouponUserCommand.GetByUserId(userId)
        val couponUsers = listOf(
            CouponUser(
                couponUserId = "coupon1",
                userId = userId,
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = "coupon2",
                userId = userId,
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        `when`(couponUserService.getAllCouponsByUserId(serviceCommand)).thenReturn(couponUsers)
        
        // when
        val result = couponUserFacade.getAllCouponsByUserId(criteria)
        
        // then
        assertThat(result.couponUsers).hasSize(2)
        assertThat(result.couponUsers[0].couponUserId).isEqualTo("coupon1")
        assertThat(result.couponUsers[0].benefitMethod).isEqualTo(CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT)
        assertThat(result.couponUsers[0].benefitAmount).isEqualTo("1000")
        assertThat(result.couponUsers[1].couponUserId).isEqualTo("coupon2")
        assertThat(result.couponUsers[1].benefitMethod).isEqualTo(CouponUserBenefitMethod.DISCOUNT_PERCENTAGE)
        assertThat(result.couponUsers[1].benefitAmount).isEqualTo("10")
        
        verify(couponUserService, times(1)).getAllCouponsByUserId(serviceCommand)
    }
    
    @Test
    @DisplayName("쿠폰 ID로 쿠폰을 조회한다")
    fun getCouponUser() {
        // given
        val couponUserId = "coupon1"
        val criteria = CouponUserCriteria.GetById(couponUserId)
        val serviceCommand = CouponUserCommand.GetById(couponUserId)
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user1",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        `when`(couponUserService.getCouponUser(serviceCommand)).thenReturn(couponUser)
        
        // when
        val result = couponUserFacade.getCouponUser(criteria)
        
        // then
        assertThat(result.couponUserId).isEqualTo(couponUserId)
        assertThat(result.benefitMethod).isEqualTo(CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT)
        assertThat(result.benefitAmount).isEqualTo("1000")
        
        verify(couponUserService, times(1)).getCouponUser(serviceCommand)
    }
    
    @Test
    @DisplayName("쿠폰을 발급한다")
    fun issueCoupon() {
        // given
        val userId = "user1"
        val benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT
        val benefitAmount = "1000"
        val criteria = CouponUserCriteria.Create(userId, benefitMethod.name, benefitAmount)
        val serviceCommand = CouponUserCommand.Create(userId, benefitMethod, benefitAmount)
        
        val createdCouponUser = CouponUser(
            couponUserId = "new-coupon-id",
            userId = userId,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            usedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        `when`(couponUserService.create(serviceCommand)).thenReturn(createdCouponUser)
        
        // when
        val result = couponUserFacade.issueCoupon(criteria)
        
        // then
        assertThat(result.couponUserId).isEqualTo("new-coupon-id")
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.benefitMethod).isEqualTo(benefitMethod)
        assertThat(result.benefitAmount).isEqualTo(benefitAmount)
        
        verify(couponUserService, times(1)).create(serviceCommand)
    }
    
    @Test
    @DisplayName("쿠폰을 사용한다")
    fun useCoupon() {
        // given
        val couponUserId = "coupon1"
        val criteria = CouponUserCriteria.Use(couponUserId)
        val serviceCommand = CouponUserCommand.Use(couponUserId)
        val couponUser = CouponUser(
            couponUserId = couponUserId,
            userId = "user1",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        `when`(couponUserService.useCoupon(serviceCommand)).thenReturn(couponUser)
        
        // when
        val result = couponUserFacade.useCoupon(criteria)
        
        // then
        assertThat(result.couponUserId).isEqualTo(couponUserId)
        assertThat(result.usedAt).isNotNull()
        
        verify(couponUserService, times(1)).useCoupon(serviceCommand)
    }
    
    @Test
    @DisplayName("쿠폰으로 할인 금액을 계산한다")
    fun calculateDiscountAmount() {
        // given
        val couponUserId = "coupon1"
        val originalAmount = 10000L
        val criteria = CouponUserCriteria.CalculateDiscount(couponUserId, originalAmount)
        val serviceCommand = CouponUserCommand.CalculateDiscount(couponUserId, originalAmount)
        val discountAmount = 1000L
        
        `when`(couponUserService.calculateDiscountAmount(serviceCommand)).thenReturn(discountAmount)
        
        // when
        val result = couponUserFacade.calculateDiscountAmount(criteria)
        
        // then
        assertThat(result).isEqualTo(discountAmount)
        
        verify(couponUserService, times(1)).calculateDiscountAmount(serviceCommand)
    }
    
    @Test
    @DisplayName("모든 쿠폰을 조회한다")
    fun getAllCoupons() {
        // given
        val criteria = CouponUserCriteria.GetAll()
        val couponUsers = listOf(
            CouponUser(
                couponUserId = "coupon1",
                userId = "user1",
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponUser(
                couponUserId = "coupon2",
                userId = "user2",
                benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        `when`(couponUserService.getAllCouponUsers(any())).thenReturn(couponUsers)
        
        // when
        val result = couponUserFacade.getAllCoupons(criteria)
        
        // then
        assertThat(result.couponUsers).hasSize(2)
        assertThat(result.couponUsers[0].couponUserId).isEqualTo("coupon1")
        assertThat(result.couponUsers[1].couponUserId).isEqualTo("coupon2")
        
        verify(couponUserService, times(1)).getAllCouponUsers(any())
    }
} 