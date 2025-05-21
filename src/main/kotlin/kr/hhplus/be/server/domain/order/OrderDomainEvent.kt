package kr.hhplus.be.server.domain.order

/**
 * 주문 도메인 이벤트
 */
sealed interface OrderDomainEvent {
    data class OrderCompleted(
        val orderId: String,
        val userId: String,
        val totalAmount: Long,
        val finalAmount: Long,
        val paymentId: String
    ) : OrderDomainEvent
} 