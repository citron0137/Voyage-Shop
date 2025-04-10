package kr.hhplus.be.server.domain.payment

interface PaymentRepository {
    fun create(payment: Payment): Payment
    fun findById(paymentId: String): Payment?
    fun findByUserId(userId: String): List<Payment>
    fun findAll(): List<Payment>
} 