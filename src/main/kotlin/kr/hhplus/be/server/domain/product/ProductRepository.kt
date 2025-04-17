package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.userpoint.UserPoint

interface ProductRepository {
    fun create(product: Product): Product
    fun findById(productId: String): Product?
    fun findByIdWithLock(id: String): Product?
    fun findAll(): List<Product>
    fun update(product: Product): Product
}