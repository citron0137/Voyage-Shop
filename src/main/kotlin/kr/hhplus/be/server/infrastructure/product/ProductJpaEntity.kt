package kr.hhplus.be.server.infrastructure.product

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.product.Product
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Product 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "products")
data class ProductJpaEntity(
    @Id
    val productId: String,
    
    val name: String,
    
    val price: Long,
    
    val stock: Long,
    
    @CreationTimestamp
    val createdAt: LocalDateTime,
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime
) {
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toDomain(): Product {
        return Product(
            productId = productId,
            name = name,
            price = price,
            stock = stock,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun fromDomain(domain: Product): ProductJpaEntity {
            return ProductJpaEntity(
                productId = domain.productId,
                name = domain.name,
                price = domain.price,
                stock = domain.stock,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
} 