package kr.hhplus.be.server.domain.payment

class PaymentCommand {
    class Create(
        val userId: String,
        val totalPaymentAmount: Long
    ) {
        init {
            if (userId.isBlank()) throw PaymentException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
            if (totalPaymentAmount <= 0) throw PaymentException.PaymentAmountShouldMoreThan0("결제 금액은 0보다 커야합니다.")
        }
    }
    
    class GetByUserId(
        val userId: String
    ) {
        init {
            if (userId.isBlank()) throw PaymentException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
        }
    }
    
    class GetById(
        val paymentId: String
    ) {
        init {
            if (paymentId.isBlank()) throw PaymentException.PaymentIdShouldNotBlank("결제 ID는 비어있을 수 없습니다.")
        }
    }
} 