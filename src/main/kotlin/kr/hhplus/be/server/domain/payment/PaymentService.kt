package kr.hhplus.be.server.domain.payment

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class PaymentService(
    private val repository: PaymentRepository
) {
    fun createPayment(command: PaymentCommand.Create): Payment {
        val paymentId = UUID.randomUUID().toString()
        val payment = Payment(
            paymentId = paymentId,
            userId = command.userId,
            totalPaymentAmount = command.totalPaymentAmount
        )
        return repository.create(payment)
    }

    fun getPaymentById(command: PaymentCommand.GetById): Payment {
        return repository.findById(command.paymentId)
            ?: throw PaymentException.NotFound("Payment with id: ${command.paymentId}")
    }

    fun getPaymentsByUserId(command: PaymentCommand.GetByUserId): List<Payment> {
        return repository.findByUserId(command.userId)
    }

    fun getAllPayments(): List<Payment> {
        return repository.findAll()
    }
} 