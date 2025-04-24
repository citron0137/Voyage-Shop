package kr.hhplus.be.server.domain.couponuser

import kr.hhplus.be.server.domain.couponuser.CouponUserException

enum class CouponUserBenefitMethod(val value: String) {
    DISCOUNT_FIXED_AMOUNT("DISCOUNT_FIXED_AMOUNT"),
    DISCOUNT_PERCENTAGE("DISCOUNT_PERCENTAGE");

    companion object {
        fun from(value: String): CouponUserBenefitMethod {
            return values().find { it.value == value }
                ?: throw CouponUserException.InvalidBenefitMethod(value)
        }
    }
} 