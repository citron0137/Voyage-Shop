package kr.hhplus.be.server.controller.couponevent.request


enum class CreateCouponEventRequestDTOCouponType{
    DISCOUNT_FIXED_AMOUNT,
    DISCOUNT_PERCENTAGE,
}

class CreateCouponEventRequest (
    val couponType: CreateCouponEventRequestDTOCouponType,
    val fixedDiscountAmount: Long?,
    val discountPercentage: Int,
    val initialStock: Long,
)

