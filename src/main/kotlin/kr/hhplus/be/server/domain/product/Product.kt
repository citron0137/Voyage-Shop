package kr.hhplus.be.server.domain.product

data class Product (
    val productId: String,
    val name: String,
    val price: Long,
    var stock: Long,
)