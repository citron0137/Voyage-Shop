package kr.hhplus.be.server.domain.couponuser

interface CouponUserDiscountCalculator {
    fun calculate(originalAmount: Long, discountAmount: String): Long
}

class CouponUserFixedAmountDiscountCalculator : CouponUserDiscountCalculator {
    override fun calculate(originalAmount: Long, discountAmount: String): Long {
        val discount = discountAmount.toLong()
        if (discount > originalAmount) {
            throw CouponUserException.DiscountAmountExceedsOriginalAmount(
                "Discount amount $discount exceeds original amount $originalAmount"
            )
        }
        return discount
    }
}

class CouponUserPercentageDiscountCalculator : CouponUserDiscountCalculator {
    override fun calculate(originalAmount: Long, discountAmount: String): Long {
        val percentage = discountAmount.toLong()
        if (percentage > 100) {
            throw CouponUserException.DiscountPercentageExceeds100(
                "Discount percentage $percentage exceeds 100%"
            )
        }
        return (originalAmount * percentage / 100)
    }
} 