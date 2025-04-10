package kr.hhplus.be.server.domain.coupon.discount

import kr.hhplus.be.server.domain.coupon.CouponException

interface DiscountCalculator {
    fun calculate(originalAmount: Long, discountAmount: String): Long
}

class FixedAmountDiscountCalculator : DiscountCalculator {
    override fun calculate(originalAmount: Long, discountAmount: String): Long {
        val discount = discountAmount.toLong()
        if (discount > originalAmount) {
            throw CouponException.DiscountAmountExceedsOriginalAmount(
                "Discount amount $discount exceeds original amount $originalAmount"
            )
        }
        return discount
    }
}

class PercentageDiscountCalculator : DiscountCalculator {
    override fun calculate(originalAmount: Long, discountAmount: String): Long {
        val percentage = discountAmount.toLong()
        if (percentage > 100) {
            throw CouponException.DiscountPercentageExceeds100(
                "Discount percentage $percentage exceeds 100%"
            )
        }
        return (originalAmount * percentage / 100)
    }
} 