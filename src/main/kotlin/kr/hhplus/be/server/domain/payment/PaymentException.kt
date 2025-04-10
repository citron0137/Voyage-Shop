package kr.hhplus.be.server.domain.payment

class PaymentException {
    class NotFound(message: String): RuntimeException("payment not found: $message")
    class PaymentAmountShouldMoreThan0(message: String): RuntimeException("payment amount should more than 0: $message")
    class UserIdShouldNotBlank(message: String): RuntimeException("user id should not blank: $message")
    class PaymentIdShouldNotBlank(message: String): RuntimeException("payment id should not blank: $message")
} 