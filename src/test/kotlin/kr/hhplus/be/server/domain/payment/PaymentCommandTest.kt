package kr.hhplus.be.server.domain.payment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentCommandTest {

    @Test
    @DisplayName("Create 커맨드를 정상적으로 생성할 수 있다")
    fun `Create 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        val totalPaymentAmount = 10000L
        
        // when
        val command = PaymentCommand.Create(userId, totalPaymentAmount)
        
        // then
        assertEquals(userId, command.userId)
        assertEquals(totalPaymentAmount, command.totalPaymentAmount)
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 사용자 ID가 비어있으면 예외가 발생한다")
    fun `Create 커맨드 생성 시 사용자 ID가 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<PaymentException.UserIdShouldNotBlank> {
            PaymentCommand.Create("", 10000L)
        }
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 결제 금액이 0 이하면 예외가 발생한다")
    fun `Create 커맨드 생성 시 결제 금액이 0 이하면 예외가 발생한다`() {
        // when & then
        assertThrows<PaymentException.PaymentAmountShouldMoreThan0> {
            PaymentCommand.Create("test-user-id", 0L)
        }
        
        assertThrows<PaymentException.PaymentAmountShouldMoreThan0> {
            PaymentCommand.Create("test-user-id", -1L)
        }
    }
    
    @Test
    @DisplayName("GetByUserId 커맨드를 정상적으로 생성할 수 있다")
    fun `GetByUserId 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        
        // when
        val command = PaymentCommand.GetByUserId(userId)
        
        // then
        assertEquals(userId, command.userId)
    }
    
    @Test
    @DisplayName("GetByUserId 커맨드 생성 시 사용자 ID가 비어있으면 예외가 발생한다")
    fun `GetByUserId 커맨드 생성 시 사용자 ID가 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<PaymentException.UserIdShouldNotBlank> {
            PaymentCommand.GetByUserId("")
        }
    }
    
    @Test
    @DisplayName("GetById 커맨드를 정상적으로 생성할 수 있다")
    fun `GetById 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val paymentId = "test-payment-id"
        
        // when
        val command = PaymentCommand.GetById(paymentId)
        
        // then
        assertEquals(paymentId, command.paymentId)
    }
    
    @Test
    @DisplayName("GetById 커맨드 생성 시 결제 ID가 비어있으면 예외가 발생한다")
    fun `GetById 커맨드 생성 시 결제 ID가 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<PaymentException.PaymentIdShouldNotBlank> {
            PaymentCommand.GetById("")
        }
    }
} 