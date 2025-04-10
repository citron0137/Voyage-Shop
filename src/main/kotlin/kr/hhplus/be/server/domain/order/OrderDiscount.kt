package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

data class OrderDiscount(
    val orderDiscountId: String,
    val orderId: String,
    val discountType: DiscountType,
    val discountId: String,
    val discountAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val DISCOUNT_TYPE_COUPON = "COUPON"
    }
} 