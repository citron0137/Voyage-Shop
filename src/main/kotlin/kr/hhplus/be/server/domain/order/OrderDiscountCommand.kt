package kr.hhplus.be.server.domain.order

class OrderDiscountCommand {
    data class Create(
        val discountType: DiscountType,
        val discountId: String,
        val discountAmount: Long
    ) {
        init {
            if (discountId.isBlank()) throw OrderException.DiscountIdShouldNotBlank("할인 ID는 비어있을 수 없습니다.")
            if (discountAmount < 0) throw OrderException.DiscountAmountShouldNotNegative("할인 금액은 음수가 될 수 없습니다.")
        }
    }
    
    data class GetByOrderId(
        val orderId: String
    ) {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
} 