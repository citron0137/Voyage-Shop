package kr.hhplus.be.server.controller.couponevent.response

enum class CouponEventResponseDTOCouponType{
    DISCOUNT_FIXED_AMOUNT,
    DISCOUNT_PERCENTAGE,
}

class CouponEventResponseDTO (
    val id: String,
    val couponType: CouponEventResponseDTOCouponType,
    val fixedDiscountAmount: Long?,
    val discountPercentage: Int?,
    val initialStock: Long,
    val currentStock: Long,
)