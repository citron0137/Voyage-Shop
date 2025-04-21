package kr.hhplus.be.server.domain.couponuser

object CouponUserDiscountCalculatorFactory {
    private val fixedAmountCalculator = CouponUserFixedAmountDiscountCalculator()
    private val percentageCalculator = CouponUserPercentageDiscountCalculator()
    
    fun getCalculator(benefitMethod: CouponUserBenefitMethod): CouponUserDiscountCalculator {
        return when (benefitMethod) {
            CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT -> fixedAmountCalculator
            CouponUserBenefitMethod.DISCOUNT_PERCENTAGE -> percentageCalculator
        }
    }
} 