package kr.hhplus.be.server.domain.order

class OrderException {
    // 존재 여부 관련 예외
    class NotFound(message: String): RuntimeException("order not found: $message")
    class OrderItemNotFound(message: String): RuntimeException("order item not found: $message")
    class OrderDiscountNotFound(message: String): RuntimeException("order discount not found: $message")
    
    // 금액/수량 관련 예외
    class AmountShouldMoreThan0(message: String): RuntimeException("amount should more than 0: $message")
    class TotalAmountShouldMoreThan0(message: String): RuntimeException("total amount should more than 0: $message")
    class DiscountAmountShouldNotNegative(message: String): RuntimeException("discount amount should not negative: $message")
    class FinalAmountShouldMoreThan0(message: String): RuntimeException("final amount should more than 0: $message")
    
    // 필수 필드 관련 예외
    class UserIdShouldNotBlank(message: String): RuntimeException("user id should not blank: $message")
    class PaymentIdShouldNotBlank(message: String): RuntimeException("payment id should not blank: $message")
    class OrderIdShouldNotBlank(message: String): RuntimeException("order id should not blank: $message")
    class ProductIdShouldNotBlank(message: String): RuntimeException("product id should not blank: $message")
    class DiscountTypeShouldNotBlank(message: String): RuntimeException("discount type should not blank: $message")
    class DiscountIdShouldNotBlank(message: String): RuntimeException("discount id should not blank: $message")
    
    // 주문 항목 관련 예외
    class OrderItemRequired(message: String): RuntimeException("order item required: $message")
    
    // 유효성 검사 예외
    class InvalidDiscountType(message: String): RuntimeException("invalid discount type: $message")
} 