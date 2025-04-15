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
class OrderItemEntity(
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
    companion object {
        /**
         * 도메인 객체를 엔티티로 변환
         */
        fun of(orderItem: OrderItem): OrderItemEntity {
            return OrderItemEntity(
                orderItemId = orderItem.orderItemId,
                orderId = orderItem.orderId,
                productId = orderItem.productId,
                amount = orderItem.amount,
                unitPrice = orderItem.unitPrice,
                totalPrice = orderItem.totalPrice,
                createdAt = orderItem.createdAt,
                updatedAt = orderItem.updatedAt
            )
        }

        /**
         * 엔티티를 도메인 객체로 변환
         */
        fun toDomain(entity: OrderItemEntity): OrderItem {
            return OrderItem(
                orderItemId = entity.orderItemId,
                orderId = entity.orderId,
                productId = entity.productId,
                amount = entity.amount,
                unitPrice = entity.unitPrice,
                totalPrice = entity.totalPrice,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
} 