package kr.hhplus.be.server.infrastructure.order

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.order.Order
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Order 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "orders")
data class OrderEntity(
    @Id
    val orderId: String,
    
    val userId: String,
    
    val paymentId: String,
    
    val totalAmount: Long,
    
    val totalDiscountAmount: Long,
    
    val finalAmount: Long,
    
    @CreationTimestamp
    val createdAt: LocalDateTime,
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun from(order: Order): OrderEntity {
            return OrderEntity(
                orderId = order.orderId,
                userId = order.userId,
                paymentId = order.paymentId,
                totalAmount = order.totalAmount,
                totalDiscountAmount = order.totalDiscountAmount,
                finalAmount = order.finalAmount,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }
    
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toOrder(): Order {
        return Order(
            orderId = orderId,
            userId = userId,
            paymentId = paymentId,
            totalAmount = totalAmount,
            totalDiscountAmount = totalDiscountAmount,
            finalAmount = finalAmount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
} 