package kr.hhplus.be.server.domain.couponuser

import kr.hhplus.be.server.shared.exception.AbstractDomainException

/**
 * 쿠폰 관련 예외 클래스
 */
sealed class CouponUserException {
    /**
     * 요청한 쿠폰을 찾을 수 없을 때 발생하는 예외
     */
    class NotFound(message: String) : AbstractDomainException(
        errorCode = "COUPON_NOT_FOUND",
        errorMessage = "coupon not found: $message"
    )
    
    /**
     * 이미 사용된 쿠폰을 사용하려고 할 때 발생하는 예외
     */
    class AlreadyUsed(message: String) : AbstractDomainException(
        errorCode = "COUPON_ALREADY_USED",
        errorMessage = "coupon already used: $message"
    )
    
    /**
     * 유저 ID가 빈 값일 때 발생하는 예외
     */
    class UserIdShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_USER_ID",
        errorMessage = "user id should not blank: $message"
    )
    
    /**
     * 혜택 방식이 빈 값일 때 발생하는 예외
     */
    class BenefitMethodShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_BENEFIT_METHOD",
        errorMessage = "benefit method should not blank: $message"
    )
    
    /**
     * 유효하지 않은 혜택 방식이 입력되었을 때 발생하는 예외
     */
    class InvalidBenefitMethod(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_BENEFIT_METHOD",
        errorMessage = "invalid benefit method: $message"
    )
    
    /**
     * 혜택 금액이 빈 값일 때 발생하는 예외
     */
    class BenefitAmountShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_BENEFIT_AMOUNT",
        errorMessage = "benefit amount should not blank: $message"
    )
    
    /**
     * 혜택 금액이 0 이하일 때 발생하는 예외
     */
    class BenefitAmountShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_BENEFIT_AMOUNT",
        errorMessage = "benefit amount should more than 0: $message"
    )
    
    /**
     * 혜택 금액이 숫자가 아닐 때 발생하는 예외
     */
    class BenefitAmountShouldBeNumeric(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_BENEFIT_AMOUNT",
        errorMessage = "benefit amount should be numeric: $message"
    )
    
    /**
     * 쿠폰 사용자 ID가 빈 값일 때 발생하는 예외
     */
    class CouponUserIdShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_USER_ID",
        errorMessage = "coupon user id should not blank: $message"
    )
    
    /**
     * 할인 금액이 원래 금액을 초과할 때 발생하는 예외
     */
    class DiscountAmountExceedsOriginalAmount(message: String) : AbstractDomainException(
        errorCode = "COUPON_DISCOUNT_EXCEEDS_ORIGINAL",
        errorMessage = "discount amount exceeds original amount: $message"
    )
    
    /**
     * 할인 퍼센트가 100%를 초과할 때 발생하는 예외
     */
    class DiscountPercentageExceeds100(message: String) : AbstractDomainException(
        errorCode = "COUPON_DISCOUNT_PERCENT_EXCEEDS",
        errorMessage = "discount percentage exceeds 100%: $message"
    )
    
    /**
     * 유효하지 않은 원래 금액이 입력되었을 때 발생하는 예외
     */
    class InvalidOriginalAmount(message: String) : AbstractDomainException(
        errorCode = "COUPON_INVALID_ORIGINAL_AMOUNT",
        errorMessage = "invalid original amount: $message"
    )
} 