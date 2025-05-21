package kr.hhplus.be.server.infrastructure.product

import jakarta.persistence.Column
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
    /**
     * 상품 ID
     */
    @Id
    @Column(name = "product_id")
    val productId: String,
    
    /**
     * 상품명
     */
    @Column(name = "name")
    val name: String,
    
    /**
     * 상품 가격
     */
    @Column(name = "price")
    val price: Long,
    
    /**
     * 상품 재고
     */
    @Column(name = "stock")
    val stock: Long,
    
    /**
     * 생성 일시
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    /**
     * 수정 일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
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