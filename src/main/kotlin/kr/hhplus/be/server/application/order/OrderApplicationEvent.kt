package kr.hhplus.be.server.application.order

/**
 * 주문 애플리케이션 이벤트
 */
sealed interface OrderApplicationEvent {
    data class OrderCompleted(
        val orderId: String,
        val userId: String,
        val totalAmount: Long,
        val finalAmount: Long,
        val paymentId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : OrderApplicationEvent
} 