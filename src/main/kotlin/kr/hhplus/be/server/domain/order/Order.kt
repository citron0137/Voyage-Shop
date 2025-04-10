package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

data class Order(
    val orderId: String,
    val userId: String,
    val paymentId: String,
    val totalAmount: Long,
    val totalDiscountAmount: Long,
    val finalAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 