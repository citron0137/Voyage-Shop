package kr.hhplus.be.server.domain.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderCommandTest {

    @Test
    @DisplayName("Create 커맨드를 정상적으로 생성할 수 있다")
    fun `Create 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        val paymentId = "test-payment-id"
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 2,
                unitPrice = 1000
            ),
            OrderItemCommand.Create(
                productId = "product-2",
                amount = 1,
                unitPrice = 3000
            )
        )
        
        // when
        val command = OrderCommand.Create(userId, paymentId, orderItems)
        
        // then
        assertEquals(userId, command.userId)
        assertEquals(paymentId, command.paymentId)
        assertEquals(2, command.orderItems.size)
        assertEquals(5000, command.totalAmount) // 2*1000 + 1*3000
        assertEquals(0, command.totalDiscountAmount)
        assertEquals(5000, command.finalAmount)
    }
    
    @Test
    @DisplayName("할인이 적용된 Create 커맨드를 정상적으로 생성할 수 있다")
    fun `할인이 적용된 Create 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        val paymentId = "test-payment-id"
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 2,
                unitPrice = 1000
            ),
            OrderItemCommand.Create(
                productId = "product-2",
                amount = 1,
                unitPrice = 3000
            )
        )
        val orderDiscounts = listOf(
            OrderDiscountCommand.Create(
                orderDiscountType = OrderDiscountType.COUPON,
                discountId = "coupon-1",
                discountAmount = 500
            )
        )
        
        // when
        val command = OrderCommand.Create(userId, paymentId, orderItems, orderDiscounts)
        
        // then
        assertEquals(userId, command.userId)
        assertEquals(paymentId, command.paymentId)
        assertEquals(2, command.orderItems.size)
        assertEquals(1, command.orderDiscounts.size)
        assertEquals(5000, command.totalAmount)
        assertEquals(500, command.totalDiscountAmount)
        assertEquals(4500, command.finalAmount)
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 사용자 ID가 비어있으면 예외가 발생한다")
    fun `Create 커맨드 생성 시 사용자 ID가 비어있으면 예외가 발생한다`() {
        // given
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 2,
                unitPrice = 1000
            )
        )
        
        // when & then
        assertThrows<OrderException.UserIdShouldNotBlank> {
            OrderCommand.Create("", "test-payment-id", orderItems)
        }
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 결제 ID가 비어있으면 예외가 발생한다")
    fun `Create 커맨드 생성 시 결제 ID가 비어있으면 예외가 발생한다`() {
        // given
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 2,
                unitPrice = 1000
            )
        )
        
        // when & then
        assertThrows<OrderException.PaymentIdShouldNotBlank> {
            OrderCommand.Create("test-user-id", "", orderItems)
        }
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 주문 상품이 없으면 예외가 발생한다")
    fun `Create 커맨드 생성 시 주문 상품이 없으면 예외가 발생한다`() {
        // when & then
        assertThrows<OrderException.OrderItemRequired> {
            OrderCommand.Create("test-user-id", "test-payment-id", emptyList())
        }
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 최종 금액이 0 이하면 예외가 발생한다")
    fun `Create 커맨드 생성 시 최종 금액이 0 이하면 예외가 발생한다`() {
        // given
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 1,
                unitPrice = 1000
            )
        )
        val orderDiscounts = listOf(
            OrderDiscountCommand.Create(
                orderDiscountType = OrderDiscountType.COUPON,
                discountId = "coupon-1",
                discountAmount = 1000
            )
        )
        
        // when & then
        assertThrows<OrderException.FinalAmountShouldMoreThan0> {
            OrderCommand.Create("test-user-id", "test-payment-id", orderItems, orderDiscounts)
        }
    }
}