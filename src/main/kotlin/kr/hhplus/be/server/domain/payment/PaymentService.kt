package kr.hhplus.be.server.domain.payment

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class PaymentService(
    private val repository: PaymentRepository
) {
    /**
     * 결제를 생성합니다.
     *
     * @param command 결제 생성 명령
     * @return 생성된 결제
     */
    fun createPayment(command: PaymentCommand.Create): Payment {
        val paymentId = UUID.randomUUID().toString()
        val payment = Payment(
            paymentId = paymentId,
            userId = command.userId,
            totalPaymentAmount = command.totalPaymentAmount
        )
        return repository.create(payment)
    }

    /**
     * 결제 ID로 결제를 조회합니다.
     *
     * @param query 결제 ID로 조회 쿼리
     * @return 조회된 결제
     * @throws PaymentException.NotFound 결제를 찾을 수 없는 경우
     */
    fun getPaymentById(query: PaymentQuery.GetById): Payment {
        return repository.findById(query.paymentId)
            ?: throw PaymentException.NotFound("Payment with id: ${query.paymentId}")
    }
    
    /**
     * 사용자 ID로 결제를 조회합니다.
     *
     * @param query 사용자 ID로 결제 조회 쿼리
     * @return 조회된 결제 목록
     */
    fun getPaymentsByUserId(query: PaymentQuery.GetByUserId): List<Payment> {
        return repository.findByUserId(query.userId)
    }
    
    /**
     * 모든 결제를 조회합니다.
     *
     * @return 모든 결제 목록
     */
    fun getAllPayments(): List<Payment> {
        return repository.findAll()
    }
} 