package kr.hhplus.be.server.domain.order

/**
 * 주문 할인 관련 명령을 정의하는 sealed class
 */
sealed class OrderDiscountCommand {
    /**
     * 주문 할인 생성 명령
     */
    data class Create(
        val orderDiscountType: OrderDiscountType,
        val discountId: String,
        val discountAmount: Long
    ) : OrderDiscountCommand() {
        init {
            if (discountId.isBlank()) throw OrderException.DiscountIdShouldNotBlank("할인 ID는 비어있을 수 없습니다.")
            if (discountAmount < 0) throw OrderException.DiscountAmountShouldNotNegative("할인 금액은 음수가 될 수 없습니다.")
        }
    }
    
    /**
     * 주문 ID로 주문 할인 목록 조회 명령
     */
    data class GetByOrderId(
        val orderId: String
    ) : OrderDiscountCommand() {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
} 