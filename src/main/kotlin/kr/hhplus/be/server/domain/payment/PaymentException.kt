package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.shared.exception.AbstractDomainException

sealed class PaymentException {
    /**
     * 요청한 결제를 찾을 수 없을 때 발생하는 예외
     */
    class NotFound(message: String, val id: String? = null) : AbstractDomainException(
        errorCode = "PAYMENT_NOT_FOUND",
        errorMessage = message
    )
    
    /**
     * 결제 금액이 0보다 작거나 같을 때 발생하는 예외
     */
    class PaymentAmountShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "PAYMENT_AMOUNT_INVALID",
        errorMessage = message
    )
    
    /**
     * 사용자 ID가 비어있을 때 발생하는 예외
     */
    class UserIdShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "PAYMENT_USER_ID_BLANK",
        errorMessage = message
    )
    
    /**
     * 결제 ID가 비어있을 때 발생하는 예외
     */
    class PaymentIdShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "PAYMENT_ID_BLANK",
        errorMessage = message
    )
} 