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
data class PaymentEntity(
    @Id
    val paymentId: String,
    
    val userId: String,
    
    val totalPaymentAmount: Long,
    
    @CreationTimestamp
    val createdAt: LocalDateTime,
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun from(payment: Payment): PaymentEntity {
            return PaymentEntity(
                paymentId = payment.paymentId,
                userId = payment.userId,
                totalPaymentAmount = payment.totalPaymentAmount,
                createdAt = payment.createdAt,
                updatedAt = payment.updatedAt
            )
        }
    }
    
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toPayment(): Payment {
        return Payment(
            paymentId = paymentId,
            userId = userId,
            totalPaymentAmount = totalPaymentAmount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
} 