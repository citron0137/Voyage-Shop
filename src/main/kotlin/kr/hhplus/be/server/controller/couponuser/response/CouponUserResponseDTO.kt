package kr.hhplus.be.server.controller.couponuser.response

enum class CouponUserResponseDTOType{
    DISCOUNT_FIXED_AMOUNT,
    DISCOUNT_PERCENTAGE,
}

class CouponUserResponseDTO (
    val id: String,
    val userId: String,
    val type: CouponUserResponseDTOType,
    val discountPercentage: Int?,
    val fixedDiscountAmount: Long?,
)
