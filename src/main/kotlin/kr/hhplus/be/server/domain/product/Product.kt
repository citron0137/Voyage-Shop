package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

data class Product (
    val productId: String,
    val name: String,
    val price: Long,
    var stock: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)