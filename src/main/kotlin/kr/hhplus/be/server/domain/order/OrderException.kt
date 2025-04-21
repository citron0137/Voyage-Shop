package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.shared.exception.AbstractDomainException

/**
 * 주문 관련 도메인 예외를 정의하는 sealed class
 */
sealed class OrderException {
    // 존재 여부 관련 예외
    class NotFound(
        message: String, 
        val id: String? = null
    ) : AbstractDomainException(
        errorCode = "ORDER_NOT_FOUND",
        errorMessage = message
    )
    
    class OrderItemNotFound(
        message: String, 
        val id: String? = null
    ) : AbstractDomainException(
        errorCode = "ORDER_ITEM_NOT_FOUND",
        errorMessage = message
    )
    
    class OrderDiscountNotFound(
        message: String, 
        val id: String? = null
    ) : AbstractDomainException(
        errorCode = "ORDER_DISCOUNT_NOT_FOUND",
        errorMessage = message
    )
    
    // 금액/수량 관련 예외
    class AmountShouldMoreThan0(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_AMOUNT_INVALID",
        errorMessage = message
    )
    
    class TotalAmountShouldMoreThan0(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_TOTAL_AMOUNT_INVALID",
        errorMessage = message
    )
    
    class DiscountAmountShouldNotNegative(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_DISCOUNT_AMOUNT_INVALID",
        errorMessage = message
    )
    
    class FinalAmountShouldMoreThan0(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_FINAL_AMOUNT_INVALID",
        errorMessage = message
    )
    
    // 필수 필드 관련 예외
    class UserIdShouldNotBlank(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_USER_ID_BLANK",
        errorMessage = message
    )
    
    class PaymentIdShouldNotBlank(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_PAYMENT_ID_BLANK",
        errorMessage = message
    )
    
    class OrderIdShouldNotBlank(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_ID_BLANK",
        errorMessage = message
    )
    
    class ProductIdShouldNotBlank(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_PRODUCT_ID_BLANK",
        errorMessage = message
    )
    
    class DiscountTypeShouldNotBlank(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_DISCOUNT_TYPE_BLANK",
        errorMessage = message
    )
    
    class DiscountIdShouldNotBlank(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_DISCOUNT_ID_BLANK",
        errorMessage = message
    )
    
    // 주문 항목 관련 예외
    class OrderItemRequired(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_ITEM_REQUIRED",
        errorMessage = message
    )
    
    // 유효성 검사 예외
    class InvalidDiscountType(
        message: String
    ) : AbstractDomainException(
        errorCode = "ORDER_DISCOUNT_TYPE_INVALID",
        errorMessage = message
    )
} 