package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 PaymentRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakePaymentRepository : PaymentRepository {
    
    private val store = ConcurrentHashMap<String, Payment>()
    
    override fun create(payment: Payment): Payment {
        store[payment.paymentId] = payment
        return payment
    }
    
    override fun findById(paymentId: String): Payment? {
        return store[paymentId]
    }
    
    override fun findByUserId(userId: String): List<Payment> {
        return store.values.filter { it.userId == userId }
    }
    
    override fun findAll(): List<Payment> {
        return store.values.toList()
    }
} 