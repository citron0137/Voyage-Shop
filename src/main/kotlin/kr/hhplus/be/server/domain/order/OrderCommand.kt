package kr.hhplus.be.server.domain.order

class OrderCommand {
    data class Create(
        val userId: String,
        val paymentId: String,
        val orderItems: List<OrderItemCommand.Create>,
        val orderDiscounts: List<OrderDiscountCommand.Create> = emptyList()
    ) {
        init {
            if (userId.isBlank()) throw OrderException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
            if (paymentId.isBlank()) throw OrderException.PaymentIdShouldNotBlank("결제 ID는 비어있을 수 없습니다.")
            if (orderItems.isEmpty()) throw OrderException.OrderItemRequired("최소 1개 이상의 주문 상품이 필요합니다.")
        }
        
        val totalAmount: Long = orderItems.sumOf { it.totalPrice }
        val totalDiscountAmount: Long = orderDiscounts.sumOf { it.discountAmount }
        val finalAmount: Long = totalAmount - totalDiscountAmount
        
        init {
            if (finalAmount <= 0) throw OrderException.FinalAmountShouldMoreThan0("최종 결제 금액은 0보다 커야합니다.")
        }
    }
    
    data class GetById(
        val orderId: String
    ) {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
    
    data class GetByUserId(
        val userId: String
    ) {
        init {
            if (userId.isBlank()) throw OrderException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
        }
    }
} 