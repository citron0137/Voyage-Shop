package kr.hhplus.be.server.domain.userpoint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserPointCommandTest {

    @Test
    @DisplayName("Create 커맨드가 정상적으로 생성됩니다")
    fun `Create 커맨드가 정상적으로 생성됩니다`() {
        // given
        val userId = "test-user-id"
        
        // when
        val command = UserPointCommand.Create(userId)
        
        // then
        assertEquals(userId, command.userId)
    }
    
    @Test
    @DisplayName("Create 커맨드에 빈 userId가 주어지면 예외가 발생합니다")
    fun `Create 커맨드에 빈 userId가 주어지면 예외가 발생합니다`() {
        // when & then
        assertThrows<UserPointException.UserIdShouldNotBlank> {
            UserPointCommand.Create("")
        }
    }
    
    @Test
    @DisplayName("Charge 커맨드가 정상적으로 생성됩니다")
    fun `Charge 커맨드가 정상적으로 생성됩니다`() {
        // given
        val userId = "test-user-id"
        val amount = 1000L
        
        // when
        val command = UserPointCommand.Charge(userId, amount)
        
        // then
        assertEquals(userId, command.userId)
        assertEquals(amount, command.amount)
    }
    
    @Test
    @DisplayName("Charge 커맨드에 빈 userId가 주어지면 예외가 발생합니다")
    fun `Charge 커맨드에 빈 userId가 주어지면 예외가 발생합니다`() {
        // when & then
        assertThrows<UserPointException.UserIdShouldNotBlank> {
            UserPointCommand.Charge("", 1000L)
        }
    }
    
    @Test
    @DisplayName("Charge 커맨드에 0 이하의 금액이 주어지면 예외가 발생합니다")
    fun `Charge 커맨드에 0 이하의 금액이 주어지면 예외가 발생합니다`() {
        // when & then
        assertThrows<UserPointException.ChargeAmountShouldMoreThan0> {
            UserPointCommand.Charge("test-user-id", 0L)
        }
        
        assertThrows<UserPointException.ChargeAmountShouldMoreThan0> {
            UserPointCommand.Charge("test-user-id", -100L)
        }
    }
    
    @Test
    @DisplayName("Use 커맨드가 정상적으로 생성됩니다")
    fun `Use 커맨드가 정상적으로 생성됩니다`() {
        // given
        val userId = "test-user-id"
        val amount = 1000L
        
        // when
        val command = UserPointCommand.Use(userId, amount)
        
        // then
        assertEquals(userId, command.userId)
        assertEquals(amount, command.amount)
    }
    
    @Test
    @DisplayName("Use 커맨드에 빈 userId가 주어지면 예외가 발생합니다")
    fun `Use 커맨드에 빈 userId가 주어지면 예외가 발생합니다`() {
        // when & then
        assertThrows<UserPointException.UserIdShouldNotBlank> {
            UserPointCommand.Use("", 1000L)
        }
    }
    
    @Test
    @DisplayName("Use 커맨드에 0 이하의 금액이 주어지면 예외가 발생합니다")
    fun `Use 커맨드에 0 이하의 금액이 주어지면 예외가 발생합니다`() {
        // when & then
        assertThrows<UserPointException.UseAmountShouldMoreThan0> {
            UserPointCommand.Use("test-user-id", 0L)
        }
        
        assertThrows<UserPointException.UseAmountShouldMoreThan0> {
            UserPointCommand.Use("test-user-id", -100L)
        }
    }
} 