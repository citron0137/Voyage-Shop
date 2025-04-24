package kr.hhplus.be.server.domain.order

/**
 * 주문 상품 관련 명령을 정의하는 sealed class
 */
sealed class OrderItemCommand {
    /**
     * 주문 상품 생성 명령
     */
    data class Create(
        val productId: String,
        val amount: Long,
        val unitPrice: Long
    ) : OrderItemCommand() {
        init {
            if (productId.isBlank()) throw OrderException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount <= 0) throw OrderException.AmountShouldMoreThan0("주문 수량은 0보다 커야합니다.")
            if (unitPrice <= 0) throw OrderException.AmountShouldMoreThan0("단가는 0보다 커야합니다.")
        }
        
        val totalPrice: Long = amount * unitPrice
    }
    
    /**
     * 주문 ID로 주문 상품 목록 조회 명령
     */
    data class GetByOrderId(
        val orderId: String
    ) : OrderItemCommand() {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
} 