package kr.hhplus.be.server.domain.product

interface ProductRepository {
    fun create(product: Product): Product
    fun findById(productId: String): Product?
    fun findByIdWithLock(id: String): Product?
    fun findAll(): List<Product>
    fun update(product: Product): Product
}