package kr.hhplus.be.server.domain.payment

/**
 * 결제 도메인 명령 관련 클래스
 */
sealed class PaymentCommand {
    /**
     * 결제 생성 명령
     */
    data class Create(
        val userId: String,
        val totalPaymentAmount: Long
    ) : PaymentCommand() {
        init {
            if (userId.isBlank()) throw PaymentException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
            if (totalPaymentAmount <= 0) throw PaymentException.PaymentAmountShouldMoreThan0("결제 금액은 0보다 커야합니다.")
        }
    }
} 