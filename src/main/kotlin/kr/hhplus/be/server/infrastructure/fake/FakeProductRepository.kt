package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 ProductRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeProductRepository : ProductRepository {
    
    private val store = ConcurrentHashMap<String, Product>()
    
    override fun create(product: Product): Product {
        store[product.productId] = product
        return product
    }
    
    override fun findById(productId: String): Product? {
        return store[productId]
    }
    
    override fun findAll(): List<Product> {
        return store.values.toList()
    }
    
    override fun update(product: Product): Product {
        store[product.productId] = product
        return product
    }
} 