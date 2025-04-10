package kr.hhplus.be.server.domain.coupon.discount

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod

object DiscountCalculatorFactory {
    private val fixedAmountCalculator = FixedAmountDiscountCalculator()
    private val percentageCalculator = PercentageDiscountCalculator()
    
    fun getCalculator(benefitMethod: CouponBenefitMethod): DiscountCalculator {
        return when (benefitMethod) {
            CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT -> fixedAmountCalculator
            CouponBenefitMethod.DISCOUNT_PERCENTAGE -> percentageCalculator
        }
    }
} 