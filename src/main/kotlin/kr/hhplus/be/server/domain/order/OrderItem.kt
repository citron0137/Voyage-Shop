package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

data class OrderItem(
    val orderItemId: String,
    val orderId: String,
    val productId: String,
    val amount: Long,
    val unitPrice: Long,
    val totalPrice: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 