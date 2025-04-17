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
class OrderDiscountEntity(
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
    companion object {
        /**
         * 도메인 객체를 엔티티로 변환
         */
        fun of(orderDiscount: OrderDiscount): OrderDiscountEntity {
            return OrderDiscountEntity(
                orderDiscountId = orderDiscount.orderDiscountId,
                orderId = orderDiscount.orderId,
                discountType = orderDiscount.discountType,
                discountId = orderDiscount.discountId,
                discountAmount = orderDiscount.discountAmount,
                createdAt = orderDiscount.createdAt,
                updatedAt = orderDiscount.updatedAt
            )
        }

        /**
         * 엔티티를 도메인 객체로 변환
         */
        fun toDomain(entity: OrderDiscountEntity): OrderDiscount {
            return OrderDiscount(
                orderDiscountId = entity.orderDiscountId,
                orderId = entity.orderId,
                discountType = entity.discountType,
                discountId = entity.discountId,
                discountAmount = entity.discountAmount,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
} 