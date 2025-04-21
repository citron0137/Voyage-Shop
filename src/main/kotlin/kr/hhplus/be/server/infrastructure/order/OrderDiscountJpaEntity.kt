package kr.hhplus.be.server.infrastructure.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.order.DiscountType
import kr.hhplus.be.server.domain.order.OrderDiscount
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * OrderDiscount 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "order_discounts")
data class OrderDiscountJpaEntity(
    @Id
    @Column(name = "order_discount_id", length = 36, nullable = false)
    val orderDiscountId: String,
    
    @Column(name = "order_id", length = 36, nullable = false)
    val orderId: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    val discountType: DiscountType,
    
    @Column(name = "discount_id", length = 36, nullable = false)
    val discountId: String,
    
    @Column(name = "discount_amount", nullable = false)
    val discountAmount: Long,
    
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
    fun toDomain(): OrderDiscount {
        return OrderDiscount(
            orderDiscountId = orderDiscountId,
            orderId = orderId,
            discountType = discountType,
            discountId = discountId,
            discountAmount = discountAmount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun fromDomain(domain: OrderDiscount): OrderDiscountJpaEntity {
            return OrderDiscountJpaEntity(
                orderDiscountId = domain.orderDiscountId,
                orderId = domain.orderId,
                discountType = domain.discountType,
                discountId = domain.discountId,
                discountAmount = domain.discountAmount,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
} 