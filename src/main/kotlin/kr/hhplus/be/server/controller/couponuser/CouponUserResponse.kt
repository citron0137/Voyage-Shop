package kr.hhplus.be.server.controller.couponuser

/**
 * 쿠폰 사용자 응답 유형
 */
sealed class CouponUserResponse {
    /**
     * 쿠폰 유형
     */
    enum class Type {
        DISCOUNT_FIXED_AMOUNT,
        DISCOUNT_PERCENTAGE,
    }
    
    /**
     * 쿠폰 사용자 정보
     */
    data class Single(
        val id: String,
        val userId: String,
        val type: Type,
        val discountPercentage: Int?,
        val fixedDiscountAmount: Long?
    ) : CouponUserResponse()
    
    /**
     * 쿠폰 사용자 목록
     */
    data class List(
        val items: kotlin.collections.List<Single>
    ) : CouponUserResponse() {
        companion object {
            fun from(items: kotlin.collections.List<Single>): List {
                return List(items)
            }
        }
    }
} 