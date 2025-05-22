package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * ProductRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Transactional
class ProductRepositoryImpl(private val productJpaRepository: ProductJpaRepository) : ProductRepository {
    
    override fun create(product: Product): Product {
        val productEntity = ProductJpaEntity.fromDomain(product)
        return productJpaRepository.save(productEntity).toDomain()
    }
    
    override fun findById(productId: String): Product? {
        return productJpaRepository.findByIdOrNull(productId)?.toDomain()
    }
    
    override fun findByIdWithLock(id: String): Product? {
        return productJpaRepository.findByIdWithLock(id)?.toDomain()
    }
    
    override fun findAll(): List<Product> {
        return productJpaRepository.findAll().map { it.toDomain() }
    }
    
    override fun update(product: Product): Product {
        val productEntity = ProductJpaEntity.fromDomain(product)
        return productJpaRepository.save(productEntity).toDomain()
    }
} 