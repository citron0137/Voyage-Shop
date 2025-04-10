package kr.hhplus.be.server.domain.coupon

enum class CouponBenefitMethod(val value: String) {
    DISCOUNT_FIXED_AMOUNT("DISCOUNT_FIXED_AMOUNT"),
    DISCOUNT_PERCENTAGE("DISCOUNT_PERCENTAGE");

    companion object {
        fun from(value: String): CouponBenefitMethod {
            return values().find { it.value == value }
                ?: throw CouponException.InvalidBenefitMethod(value)
        }
    }
} 