package kr.hhplus.be.server.integration.coupon

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.couponuser.CouponUserCriteria
import kr.hhplus.be.server.application.couponuser.CouponUserFacade
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import kr.hhplus.be.server.domain.couponuser.CouponUserException
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@DisplayName("쿠폰 유저 통합 테스트")
class CouponUserFacadeIntegrationTest {

    @Autowired
    private lateinit var couponUserFacade: CouponUserFacade

    @Autowired
    private lateinit var userService: UserService
    
    private val testUsers = mutableListOf<User>()
    
    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 생성
        for (i in 1..10) {
            val user = userService.createUser()
            testUsers.add(user)
        }
    }

    @Test
    @DisplayName("사용자에게 쿠폰을 발급할 수 있다")
    @Transactional
    fun issueCouponTest() {
        // given
        val userId = testUsers[0].userId
        val benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT
        val benefitAmount = "1000"
        val criteria = CouponUserCriteria.Create(userId, benefitMethod.name, benefitAmount)

        // when
        val couponUser = couponUserFacade.issueCoupon(criteria)

        // then
        assertThat(couponUser.couponUserId).isNotNull()
        assertThat(couponUser.userId).isEqualTo(userId)
        assertThat(couponUser.benefitMethod).isEqualTo(benefitMethod)
        assertThat(couponUser.benefitAmount).isEqualTo(benefitAmount)
        assertThat(couponUser.usedAt).isNull()
    }

    @Test
    @DisplayName("발급된 쿠폰을 사용할 수 있다")
    @Transactional
    fun useCouponTest() {
        // given
        val userId = testUsers[1].userId
        val benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT
        val benefitAmount = "2000"
        
        val issueCriteria = CouponUserCriteria.Create(userId, benefitMethod.name, benefitAmount)
        val issuedCoupon = couponUserFacade.issueCoupon(issueCriteria)
        
        val useCriteria = CouponUserCriteria.Use(issuedCoupon.couponUserId)
        
        // when
        val usedCoupon = couponUserFacade.useCoupon(useCriteria)
        
        // then
        assertThat(usedCoupon.couponUserId).isEqualTo(issuedCoupon.couponUserId)
        assertThat(usedCoupon.usedAt).isNotNull()
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 사용하면 예외가 발생한다")
    @Transactional
    fun useAlreadyUsedCouponTest() {
        // given
        val userId = testUsers[2].userId
        val issueCriteria = CouponUserCriteria.Create(
            userId, 
            CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT.name,
            "3000"
        )
        val issuedCoupon = couponUserFacade.issueCoupon(issueCriteria)
        
        // 쿠폰 사용
        val useCriteria = CouponUserCriteria.Use(issuedCoupon.couponUserId)
        couponUserFacade.useCoupon(useCriteria)
        
        // when & then
        assertThrows<CouponUserException.AlreadyUsed> {
            couponUserFacade.useCoupon(useCriteria)
        }
    }

    @Test
    @DisplayName("쿠폰으로 고정 금액 할인을 계산할 수 있다")
    @Transactional
    fun calculateFixedAmountDiscountTest() {
        // given
        val userId = testUsers[3].userId
        val benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT
        val benefitAmount = "2000"
        val originalAmount = 10000L
        
        val issueCriteria = CouponUserCriteria.Create(userId, benefitMethod.name, benefitAmount)
        val coupon = couponUserFacade.issueCoupon(issueCriteria)
        
        val calculateCriteria = CouponUserCriteria.CalculateDiscount(coupon.couponUserId, originalAmount)
        
        // when
        val discountAmount = couponUserFacade.calculateDiscountAmount(calculateCriteria)
        
        // then
        assertThat(discountAmount).isEqualTo(2000L)
    }

    @Test
    @DisplayName("쿠폰으로 퍼센트 할인을 계산할 수 있다")
    @Transactional
    fun calculatePercentageDiscountTest() {
        // given
        val userId = testUsers[4].userId
        val benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE
        val benefitAmount = "10" // 10% 할인
        val originalAmount = 10000L
        
        val issueCriteria = CouponUserCriteria.Create(userId, benefitMethod.name, benefitAmount)
        val coupon = couponUserFacade.issueCoupon(issueCriteria)
        
        val calculateCriteria = CouponUserCriteria.CalculateDiscount(coupon.couponUserId, originalAmount)
        
        // when
        val discountAmount = couponUserFacade.calculateDiscountAmount(calculateCriteria)
        
        // then
        assertThat(discountAmount).isEqualTo(1000L) // 10000의 10%는 1000
    }

    @Test
    @DisplayName("쿠폰 ID로 쿠폰 정보를 조회할 수 있다")
    @Transactional
    fun getCouponUserTest() {
        // given
        val userId = testUsers[5].userId
        val benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT
        val benefitAmount = "5000"
        
        val issueCriteria = CouponUserCriteria.Create(userId, benefitMethod.name, benefitAmount)
        val issuedCoupon = couponUserFacade.issueCoupon(issueCriteria)
        
        val getCriteria = CouponUserCriteria.GetById(issuedCoupon.couponUserId)
        
        // when
        val coupon = couponUserFacade.getCouponUser(getCriteria)
        
        // then
        assertThat(coupon.couponUserId).isEqualTo(issuedCoupon.couponUserId)
        assertThat(coupon.userId).isEqualTo(userId)
        assertThat(coupon.benefitMethod).isEqualTo(benefitMethod)
        assertThat(coupon.benefitAmount).isEqualTo(benefitAmount)
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 ID로 조회하면 예외가 발생한다")
    @Transactional
    fun getCouponUserNotFoundTest() {
        // given
        val nonExistentCouponId = "non-existent-coupon-id-${UUID.randomUUID()}"
        val getCriteria = CouponUserCriteria.GetById(nonExistentCouponId)
        
        // when & then
        assertThrows<CouponUserException.NotFound> {
            couponUserFacade.getCouponUser(getCriteria)
        }
    }

    @Test
    @DisplayName("유저 ID로 해당 유저의 모든 쿠폰을 조회할 수 있다")
    @Transactional
    fun getAllCouponsByUserIdTest() {
        // given
        val userId = testUsers[6].userId
        
        // 쿠폰 3개 발급
        val criteria1 = CouponUserCriteria.Create(userId, CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT.name, "1000")
        val criteria2 = CouponUserCriteria.Create(userId, CouponUserBenefitMethod.DISCOUNT_PERCENTAGE.name, "5")
        val criteria3 = CouponUserCriteria.Create(userId, CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT.name, "3000")
        
        couponUserFacade.issueCoupon(criteria1)
        couponUserFacade.issueCoupon(criteria2)
        couponUserFacade.issueCoupon(criteria3)
        
        val getUserCriteria = CouponUserCriteria.GetByUserId(userId)
        
        // when
        val coupons = couponUserFacade.getAllCouponsByUserId(getUserCriteria)
        
        // then
        assertThat(coupons.couponUsers).hasSize(3)
        assertThat(coupons.couponUsers.map { it.userId }).containsOnly(userId)
    }

    @Test
    @DisplayName("모든 쿠폰을 조회할 수 있다")
    @Transactional
    fun getAllCouponsTest() {
        // given
        val getAllCriteria = CouponUserCriteria.GetAll()
        val beforeCount = couponUserFacade.getAllCoupons(getAllCriteria).couponUsers.size
        
        // 쿠폰 2개 발급
        val criteria1 = CouponUserCriteria.Create(testUsers[7].userId, CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT.name, "1500")
        val criteria2 = CouponUserCriteria.Create(testUsers[8].userId, CouponUserBenefitMethod.DISCOUNT_PERCENTAGE.name, "15")
        
        couponUserFacade.issueCoupon(criteria1)
        couponUserFacade.issueCoupon(criteria2)
        
        // when
        val coupons = couponUserFacade.getAllCoupons(getAllCriteria)
        
        // then
        assertThat(coupons.couponUsers.size).isEqualTo(beforeCount + 2)
    }
} 