package kr.hhplus.be.server.controller.couponevent

/**
 * 쿠폰 이벤트 요청
 */
sealed class CouponEventRequest {
    /**
     * 쿠폰 이벤트 생성 요청
     */
    data class Create(
        val benefitMethod: String,
        val benefitAmount: String,
        val totalIssueAmount: Int
    ) : CouponEventRequest()
    
    /**
     * 쿠폰 발급 요청
     */
    data class IssueCoupon(
        val userId: String
    ) : CouponEventRequest()
} 