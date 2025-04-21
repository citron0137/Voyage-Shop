package kr.hhplus.be.server.infrastructure.payment

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.payment.Payment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Payment 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "payments")
data class PaymentJpaEntity(
    @Id
    val paymentId: String,
    
    val userId: String,
    
    val totalPaymentAmount: Long,
    
    @CreationTimestamp
    val createdAt: LocalDateTime,
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime
) {
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toDomain(): Payment {
        return Payment(
            paymentId = paymentId,
            userId = userId,
            totalPaymentAmount = totalPaymentAmount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun fromDomain(domain: Payment): PaymentJpaEntity {
            return PaymentJpaEntity(
                paymentId = domain.paymentId,
                userId = domain.userId,
                totalPaymentAmount = domain.totalPaymentAmount,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
} 