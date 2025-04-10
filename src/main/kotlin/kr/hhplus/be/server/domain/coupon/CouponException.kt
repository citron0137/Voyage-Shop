package kr.hhplus.be.server.domain.coupon

sealed class CouponException(message: String) : RuntimeException(message) {
    class NotFound(message: String) : CouponException("coupon not found: $message")
    class AlreadyUsed(message: String) : CouponException("coupon already used: $message")
    class UserIdShouldNotBlank(message: String) : CouponException("user id should not blank: $message")
    class BenefitMethodShouldNotBlank(message: String) : CouponException("benefit method should not blank: $message")
    class InvalidBenefitMethod(message: String) : CouponException("invalid benefit method: $message")
    class BenefitAmountShouldNotBlank(message: String) : CouponException("benefit amount should not blank: $message")
    class BenefitAmountShouldMoreThan0(message: String) : CouponException("benefit amount should more than 0: $message")
    class BenefitAmountShouldBeNumeric(message: String) : CouponException("benefit amount should be numeric: $message")
    class CouponUserIdShouldNotBlank(message: String) : CouponException("coupon user id should not blank: $message")
    class DiscountAmountExceedsOriginalAmount(message: String) : CouponException("discount amount exceeds original amount: $message")
    class DiscountPercentageExceeds100(message: String) : CouponException("discount percentage exceeds 100%: $message")
} 