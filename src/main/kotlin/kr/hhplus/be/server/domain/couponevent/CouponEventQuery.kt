package kr.hhplus.be.server.domain.couponevent

/**
 * 쿠폰 이벤트 조회를 위한 쿼리 클래스
 */
sealed class CouponEventQuery {
    /**
     * ID로 쿠폰 이벤트를 조회하는 쿼리
     */
    data class GetById(
        val id: String
    ) : CouponEventQuery() {
        init {
            if (id.isBlank()) {
                throw CouponEventException.InvalidId("Coupon event ID should not be blank")
            }
        }
    }
    
    /**
     * 모든 쿠폰 이벤트를 조회하는 쿼리
     */
    class GetAll : CouponEventQuery()
    
    /**
     * 발급 가능한 쿠폰 이벤트만 조회하는 쿼리
     */
    class GetAvailable : CouponEventQuery()
} 