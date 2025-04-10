package kr.hhplus.be.server.domain.payment

import java.time.LocalDateTime

data class Payment(
    val paymentId: String,
    val userId: String,
    val totalPaymentAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 