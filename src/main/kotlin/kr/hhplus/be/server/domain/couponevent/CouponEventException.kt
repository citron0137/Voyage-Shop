package kr.hhplus.be.server.domain.couponevent

import kr.hhplus.be.server.shared.exception.AbstractDomainException

/**
 * 쿠폰 이벤트 관련 예외 클래스
 */
sealed class CouponEventException {
    /**
     * 쿠폰 이벤트를 찾을 수 없을 때 발생하는 예외
     */
    class NotFound(couponEventId: String) : AbstractDomainException(
        errorCode = "COUPON_EVENT_NOT_FOUND",
        errorMessage = "coupon event not found: $couponEventId"
    )
    
    /**
     * 쿠폰 재고가 없을 때 발생하는 예외
     */
    class StockEmpty(couponEventId: String) : AbstractDomainException(
        errorCode = "COUPON_EVENT_STOCK_EMPTY",
        errorMessage = "coupon stock is empty for event: $couponEventId"
    )
    
    /**
     * 쿠폰 재고가 부족할 때 발생하는 예외
     */
    class OutOfStock(message: String) : AbstractDomainException(
        errorCode = "COUPON_EVENT_OUT_OF_STOCK",
        errorMessage = "coupon out of stock: $message"
    )
    
    /**
     * 잘못된 혜택 방식 값이 입력되었을 때 발생하는 예외
     */
    class InvalidBenefitMethod(method: String) : AbstractDomainException(
        errorCode = "COUPON_EVENT_INVALID_BENEFIT_METHOD",
        errorMessage = "invalid benefit method: $method"
    )
    
    /**
     * 잘못된 혜택 금액이 입력되었을 때 발생하는 예외
     */
    class InvalidBenefitAmount(message: String) : AbstractDomainException(
        errorCode = "COUPON_EVENT_INVALID_BENEFIT_AMOUNT",
        errorMessage = "invalid benefit amount: $message"
    )
    
    /**
     * 잘못된 발급 수량이 입력되었을 때 발생하는 예외
     */
    class InvalidIssueAmount(message: String) : AbstractDomainException(
        errorCode = "COUPON_EVENT_INVALID_ISSUE_AMOUNT",
        errorMessage = "invalid issue amount: $message"
    )
    
    /**
     * 잘못된 ID가 입력되었을 때 발생하는 예외
     */
    class InvalidId(message: String) : AbstractDomainException(
        errorCode = "COUPON_EVENT_INVALID_ID",
        errorMessage = "invalid id: $message"
    )
} 