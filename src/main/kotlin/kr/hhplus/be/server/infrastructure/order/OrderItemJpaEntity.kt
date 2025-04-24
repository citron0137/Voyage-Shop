package kr.hhplus.be.server.infrastructure.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.order.OrderItem
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * OrderItem 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "order_items")
data class OrderItemJpaEntity(
    @Id
    @Column(name = "order_item_id", length = 36, nullable = false)
    val orderItemId: String,
    
    @Column(name = "order_id", length = 36, nullable = false)
    val orderId: String,
    
    @Column(name = "product_id", length = 36, nullable = false)
    val productId: String,
    
    @Column(name = "amount", nullable = false)
    val amount: Long,
    
    @Column(name = "unit_price", nullable = false)
    val unitPrice: Long,
    
    @Column(name = "total_price", nullable = false)
    val totalPrice: Long,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toDomain(): OrderItem {
        return OrderItem(
            orderItemId = orderItemId,
            orderId = orderId,
            productId = productId,
            amount = amount,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun fromDomain(domain: OrderItem): OrderItemJpaEntity {
            return OrderItemJpaEntity(
                orderItemId = domain.orderItemId,
                orderId = domain.orderId,
                productId = domain.productId,
                amount = domain.amount,
                unitPrice = domain.unitPrice,
                totalPrice = domain.totalPrice,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
} 