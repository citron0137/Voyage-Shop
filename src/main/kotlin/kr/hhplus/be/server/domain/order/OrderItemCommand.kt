package kr.hhplus.be.server.domain.order

class OrderItemCommand {
    data class Create(
        val productId: String,
        val amount: Long,
        val unitPrice: Long
    ) {
        init {
            if (productId.isBlank()) throw OrderException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount <= 0) throw OrderException.AmountShouldMoreThan0("주문 수량은 0보다 커야합니다.")
            if (unitPrice <= 0) throw OrderException.AmountShouldMoreThan0("단가는 0보다 커야합니다.")
        }
        
        val totalPrice: Long = amount * unitPrice
    }
    
    data class GetByOrderId(
        val orderId: String
    ) {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
} 