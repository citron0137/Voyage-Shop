package kr.hhplus.be.server.controller.couponevent

/**
 * 쿠폰 이벤트 응답
 */
sealed class CouponEventResponse {
    /**
     * 쿠폰 이벤트 정보
     */
    data class Event(
        val id: String,
        val benefitMethod: String,
        val benefitAmount: String,
        val totalIssueAmount: Int,
        val leftIssueAmount: Int
    ) : CouponEventResponse()
    
    /**
     * 쿠폰 발급 응답
     */
    data class IssueCoupon(
        val couponUserId: String
    ) : CouponEventResponse()
    
    /**
     * 쿠폰 이벤트 목록
     */
    data class List(
        val items: kotlin.collections.List<Event>
    ) : CouponEventResponse() {
        companion object {
            fun from(events: kotlin.collections.List<Event>): List {
                return List(events)
            }
        }
    }
} 