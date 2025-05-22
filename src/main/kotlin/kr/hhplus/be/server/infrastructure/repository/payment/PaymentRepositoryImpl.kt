package kr.hhplus.be.server.infrastructure.payment

import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * PaymentRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class PaymentRepositoryImpl(private val paymentJpaRepository: PaymentJpaRepository) : PaymentRepository {
    
    override fun create(payment: Payment): Payment {
        val paymentEntity = PaymentJpaEntity.fromDomain(payment)
        return paymentJpaRepository.save(paymentEntity).toDomain()
    }
    
    override fun findById(paymentId: String): Payment? {
        return paymentJpaRepository.findByIdOrNull(paymentId)?.toDomain()
    }
    
    override fun findByUserId(userId: String): List<Payment> {
        return paymentJpaRepository.findByUserId(userId).map { it.toDomain() }
    }
    
    override fun findAll(): List<Payment> {
        return paymentJpaRepository.findAll().map { it.toDomain() }
    }
} 