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
data class OrderJpaEntity(
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
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toDomain(): Order {
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
    
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun fromDomain(domain: Order): OrderJpaEntity {
            return OrderJpaEntity(
                orderId = domain.orderId,
                userId = domain.userId,
                paymentId = domain.paymentId,
                totalAmount = domain.totalAmount,
                totalDiscountAmount = domain.totalDiscountAmount,
                finalAmount = domain.finalAmount,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
} 