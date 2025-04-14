package kr.hhplus.be.server.domain.payment

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID

class PaymentServiceUnitTest {
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setUp() {
        paymentRepository = mockk()
        paymentService = PaymentService(paymentRepository)
    }

    @Test
    fun `결제를 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        val totalPaymentAmount = 10000L
        val command = PaymentCommand.Create(userId, totalPaymentAmount)
        
        val expectedPayment = Payment(
            paymentId = "test-payment-id",
            userId = userId,
            totalPaymentAmount = totalPaymentAmount
        )
        
        every { paymentRepository.create(any()) } returns expectedPayment

        // when
        val result = paymentService.createPayment(command)

        // then
        verify { paymentRepository.create(any()) }
        assertEquals(expectedPayment.userId, result.userId)
        assertEquals(expectedPayment.totalPaymentAmount, result.totalPaymentAmount)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `ID로 결제를 조회할 수 있다`() {
        // given
        val paymentId = UUID.randomUUID().toString()
        
        val expectedPayment = Payment(
            paymentId = paymentId,
            userId = "test-user-id",
            totalPaymentAmount = 10000L
        )
        
        every { paymentRepository.findById(paymentId) } returns expectedPayment

        // when
        val result = paymentService.getPaymentById(paymentId)

        // then
        verify { paymentRepository.findById(paymentId) }
        assertEquals(expectedPayment, result)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 예외가 발생한다`() {
        // given
        val paymentId = UUID.randomUUID().toString()
        
        every { paymentRepository.findById(paymentId) } returns null

        // when & then
        assertThrows<PaymentException.NotFound> {
            paymentService.getPaymentById(paymentId)
        }
    }

    @Test
    fun `사용자 ID로 결제 목록을 조회할 수 있다`() {
        // given
        val userId = "test-user-id"
        
        val expectedPayments = listOf(
            Payment(
                paymentId = UUID.randomUUID().toString(),
                userId = userId,
                totalPaymentAmount = 10000L
            ),
            Payment(
                paymentId = UUID.randomUUID().toString(),
                userId = userId,
                totalPaymentAmount = 20000L
            )
        )
        
        every { paymentRepository.findByUserId(userId) } returns expectedPayments

        // when
        val results = paymentService.getPaymentsByUserId(userId)

        // then
        verify { paymentRepository.findByUserId(userId) }
        assertEquals(expectedPayments.size, results.size)
        results.forEachIndexed { index, payment ->
            assertEquals(expectedPayments[index], payment)
            assertNotNull(payment.createdAt)
            assertNotNull(payment.updatedAt)
        }
    }

    @Test
    fun `모든 결제를 조회할 수 있다`() {
        // given
        val expectedPayments = listOf(
            Payment(
                paymentId = UUID.randomUUID().toString(),
                userId = "user1",
                totalPaymentAmount = 10000L
            ),
            Payment(
                paymentId = UUID.randomUUID().toString(),
                userId = "user2",
                totalPaymentAmount = 20000L
            )
        )
        
        every { paymentRepository.findAll() } returns expectedPayments

        // when
        val results = paymentService.getAllPayments()

        // then
        verify { paymentRepository.findAll() }
        assertEquals(expectedPayments, results)
        results.forEach {
            assertNotNull(it.createdAt)
            assertNotNull(it.updatedAt)
        }
    }
} 