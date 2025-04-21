package kr.hhplus.be.server.domain.payment

import java.time.LocalDateTime

data class Payment(
    val paymentId: String,
    val userId: String,
    val totalPaymentAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // 불변성을 유지하면서 상태 변경하는 메서드
    fun updateAmount(newAmount: Long): Payment {
        if (newAmount <= 0) {
            throw PaymentException.PaymentAmountShouldMoreThan0("결제 금액은 0보다 커야합니다.")
        }
        
        return this.copy(
            totalPaymentAmount = newAmount,
            updatedAt = LocalDateTime.now()
        )
    }
} 