package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

data class Order(
    val orderId: String,
    val userId: String,
    val paymentId: String,
    val totalAmount: Long,
    val totalDiscountAmount: Long,
    val finalAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 주문 정보를 업데이트합니다.
     * 불변성을 유지하면서 새로운 Order 객체를 반환합니다.
     */
    fun update(
        userId: String = this.userId,
        paymentId: String = this.paymentId,
        totalAmount: Long = this.totalAmount,
        totalDiscountAmount: Long = this.totalDiscountAmount,
        finalAmount: Long = this.finalAmount
    ): Order {
        return this.copy(
            userId = userId,
            paymentId = paymentId,
            totalAmount = totalAmount,
            totalDiscountAmount = totalDiscountAmount,
            finalAmount = finalAmount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 주문의 총 금액을 계산합니다.
     * 아이템 목록으로부터 총 금액을 계산하여 반환합니다.
     */
    fun calculateTotalAmount(orderItems: List<OrderItem>): Long {
        return orderItems.sumOf { it.totalPrice }
    }
    
    /**
     * 주문의 총 할인 금액을 계산합니다.
     * 할인 목록으로부터 총 할인 금액을 계산하여 반환합니다.
     */
    fun calculateTotalDiscountAmount(orderDiscounts: List<OrderDiscount>): Long {
        return orderDiscounts.sumOf { it.discountAmount }
    }
    
    /**
     * 주문의 최종 결제 금액을 계산합니다.
     * 총 금액에서 총 할인 금액을 차감하여 반환합니다.
     */
    fun calculateFinalAmount(totalAmount: Long, totalDiscountAmount: Long): Long {
        val finalAmount = totalAmount - totalDiscountAmount
        if (finalAmount <= 0) {
            throw OrderException.FinalAmountShouldMoreThan0("최종 결제 금액은 0보다 커야합니다.")
        }
        return finalAmount
    }
    
    companion object {
        /**
         * 새로운 주문을 생성합니다.
         */
        fun create(
            orderId: String,
            userId: String,
            paymentId: String,
            totalAmount: Long,
            totalDiscountAmount: Long,
            finalAmount: Long
        ): Order {
            if (finalAmount <= 0) {
                throw OrderException.FinalAmountShouldMoreThan0("최종 결제 금액은 0보다 커야합니다.")
            }
            
            return Order(
                orderId = orderId,
                userId = userId,
                paymentId = paymentId,
                totalAmount = totalAmount,
                totalDiscountAmount = totalDiscountAmount,
                finalAmount = finalAmount
            )
        }
    }
} 