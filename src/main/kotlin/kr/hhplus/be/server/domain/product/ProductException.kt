package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.shared.exception.AbstractDomainException

sealed class ProductException {
    class StockAmountShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "P_INVALID_STOCK",
        errorMessage = "stock amount should more than 0: $message"
    )
    
    class NotFound(message: String) : AbstractDomainException(
        errorCode = "P_NOT_FOUND",
        errorMessage = "product not found: $message"
    )
    
    class StockAmountOverflow(message: String) : AbstractDomainException(
        errorCode = "P_STOCK_OVERFLOW",
        errorMessage = "stock amount overflow: $message"
    )
    
    class IncreaseStockAmountShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "P_INVALID_INC_STOCK",
        errorMessage = "stock amount should more than 0: $message"
    )
    
    class StockAmountUnderflow(message: String) : AbstractDomainException(
        errorCode = "P_STOCK_UNDERFLOW",
        errorMessage = "stock amount underflow: $message"
    )
    
    class DecreaseStockAmountShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "P_INVALID_DEC_STOCK",
        errorMessage = "decrease stock amount should more than 0: $message"
    )
    
    class PriceShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "P_INVALID_PRICE",
        errorMessage = "price should more than 0: $message"
    )
    
    class NameShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "P_INVALID_NAME",
        errorMessage = "name should not blank: $message"
    )
    
    class ProductIdShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "P_INVALID_ID",
        errorMessage = "product id should not blank: $message"
    )
}