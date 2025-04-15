package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * ProductRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class ProductRepositoryImpl(private val productJpaRepository: ProductJpaRepository) : ProductRepository {
    
    override fun create(product: Product): Product {
        val productEntity = ProductEntity.from(product)
        return productJpaRepository.save(productEntity).toProduct()
    }
    
    override fun findById(productId: String): Product? {
        return productJpaRepository.findByIdOrNull(productId)?.toProduct()
    }
    
    override fun findAll(): List<Product> {
        return productJpaRepository.findAll().map { it.toProduct() }
    }
    
    override fun update(product: Product): Product {
        val productEntity = ProductEntity.from(product)
        return productJpaRepository.save(productEntity).toProduct()
    }
} 