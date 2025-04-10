package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository

class ProductRepositoryImpl: ProductRepository {
    override fun create(product: Product): Product {
        TODO("Not yet implemented")
    }

    override fun findById(productId: String): Product? {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<Product> {
        TODO("Not yet implemented")
    }

    override fun update(product: Product): Product {
        TODO("Not yet implemented")
    }
}