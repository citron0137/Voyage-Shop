package kr.hhplus.be.server.domain.payment

/**
 * 결제 도메인 조회 관련 클래스
 */
sealed class PaymentQuery {
    /**
     * 사용자 ID로 결제 조회
     */
    data class GetByUserId(
        val userId: String
    ) : PaymentQuery() {
        init {
            if (userId.isBlank()) throw PaymentException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
        }
    }
    
    /**
     * 결제 ID로 결제 조회
     */
    data class GetById(
        val paymentId: String
    ) : PaymentQuery() {
        init {
            if (paymentId.isBlank()) throw PaymentException.PaymentIdShouldNotBlank("결제 ID는 비어있을 수 없습니다.")
        }
    }
    
    /**
     * 모든 결제 조회
     */
    object GetAll : PaymentQuery()
} 