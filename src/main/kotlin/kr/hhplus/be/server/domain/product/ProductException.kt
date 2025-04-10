package kr.hhplus.be.server.domain.product


class ProductException{
    class StockAmountShouldMoreThan0(message: String) : RuntimeException("stock amount should more than 0: $message")
    class NotFound(message: String): RuntimeException("product not found: $message")
    class StockAmountOverflow(message: String): RuntimeException("stock amount overflow: $message")
    class IncreaseStockAmountShouldMoreThan0(message: String): RuntimeException("stock amount should more than 0: $message")
    class StockAmountUnderflow(message: String): RuntimeException("stock amount underflow: $message")
    class DecreaseStockAmountShouldMoreThan0(message: String): RuntimeException("decrease stock amount should more than 0: $message")
    class PriceShouldMoreThan0(message: String) : RuntimeException("price should more than 0: $message")
    class NameShouldNotBlank(message: String) : RuntimeException("name should not blank: $message")
    class ProductIdShouldNotBlank(message: String) : RuntimeException("product id should not blank: $message")
}