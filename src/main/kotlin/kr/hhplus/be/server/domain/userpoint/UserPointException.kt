package kr.hhplus.be.server.domain.userpoint


class UserPointException{
    class NotFound(message: String): RuntimeException("user point not found: $message")
    class PointAmountOverflow(message: String): RuntimeException("Point overflow: $message")
    class ChargeAmountShouldMoreThan0(message: String): RuntimeException("Charge amount should more than 0: $message")

    class PointAmountUnderflow(message: String): RuntimeException("Point underflow: $message")
    class UseAmountShouldMoreThan0(message: String): RuntimeException("Use amount should more than 0: $message")
}