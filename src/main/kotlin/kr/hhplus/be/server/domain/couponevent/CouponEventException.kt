package kr.hhplus.be.server.domain.couponevent

/**
 * 쿠폰 이벤트 관련 예외 클래스
 */
sealed class CouponEventException(message: String) : RuntimeException(message) {
    /**
     * 쿠폰 이벤트를 찾을 수 없을 때 발생하는 예외
     */
    class NotFound(couponEventId: String) : CouponEventException("coupon event not found: $couponEventId")
    
    /**
     * 쿠폰 재고가 없을 때 발생하는 예외
     */
    class StockEmpty(couponEventId: String) : CouponEventException("coupon stock is empty for event: $couponEventId")
    
    /**
     * 쿠폰 재고가 부족할 때 발생하는 예외
     */
    class OutOfStock(message: String) : CouponEventException("coupon out of stock: $message")
    
    /**
     * 잘못된 혜택 방식 값이 입력되었을 때 발생하는 예외
     */
    class InvalidBenefitMethod(method: String) : CouponEventException("invalid benefit method: $method")
    
    /**
     * 잘못된 혜택 금액이 입력되었을 때 발생하는 예외
     */
    class InvalidBenefitAmount(message: String) : CouponEventException("invalid benefit amount: $message")
    
    /**
     * 잘못된 발급 수량이 입력되었을 때 발생하는 예외
     */
    class InvalidIssueAmount(message: String) : CouponEventException("invalid issue amount: $message")
    
    /**
     * 잘못된 ID가 입력되었을 때 발생하는 예외
     */
    class InvalidId(message: String) : CouponEventException("invalid id: $message")
} 