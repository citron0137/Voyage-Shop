package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

data class OrderDiscount(
    val orderDiscountId: String,
    val orderId: String,
    val type: OrderDiscountType,
    val discountId: String,
    val discountAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 주문 할인 정보를 업데이트합니다.
     * 불변성을 유지하면서 새로운 OrderDiscount 객체를 반환합니다.
     */
    fun update(
        type: OrderDiscountType = this.type,
        discountId: String = this.discountId,
        discountAmount: Long = this.discountAmount
    ): OrderDiscount {
        if (discountId.isBlank()) {
            throw OrderException.DiscountIdShouldNotBlank("할인 ID는 비어있을 수 없습니다.")
        }
        if (discountAmount < 0) {
            throw OrderException.DiscountAmountShouldNotNegative("할인 금액은 음수가 될 수 없습니다.")
        }
        
        return this.copy(
            type = type,
            discountId = discountId,
            discountAmount = discountAmount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    companion object {
        /**
         * 새로운 주문 할인을 생성합니다.
         */
        fun create(
            orderDiscountId: String,
            orderId: String,
            orderDiscountType: OrderDiscountType,
            discountId: String,
            discountAmount: Long
        ): OrderDiscount {
            if (discountId.isBlank()) {
                throw OrderException.DiscountIdShouldNotBlank("할인 ID는 비어있을 수 없습니다.")
            }
            if (discountAmount < 0) {
                throw OrderException.DiscountAmountShouldNotNegative("할인 금액은 음수가 될 수 없습니다.")
            }
            
            return OrderDiscount(
                orderDiscountId = orderDiscountId,
                orderId = orderId,
                type = orderDiscountType,
                discountId = discountId,
                discountAmount = discountAmount
            )
        }
    }
} 