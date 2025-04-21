package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

data class OrderItem(
    val orderItemId: String,
    val orderId: String,
    val productId: String,
    val amount: Long,
    val unitPrice: Long,
    val totalPrice: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 주문 상품 정보를 업데이트합니다.
     * 불변성을 유지하면서 새로운 OrderItem 객체를 반환합니다.
     */
    fun update(
        productId: String = this.productId,
        amount: Long = this.amount,
        unitPrice: Long = this.unitPrice
    ): OrderItem {
        if (amount <= 0) {
            throw OrderException.AmountShouldMoreThan0("주문 수량은 0보다 커야합니다.")
        }
        if (unitPrice <= 0) {
            throw OrderException.AmountShouldMoreThan0("단가는 0보다 커야합니다.")
        }
        
        val newTotalPrice = amount * unitPrice
        
        return this.copy(
            productId = productId,
            amount = amount,
            unitPrice = unitPrice,
            totalPrice = newTotalPrice,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 총 가격을 계산합니다.
     */
    fun calculateTotalPrice(amount: Long, unitPrice: Long): Long {
        return amount * unitPrice
    }
    
    companion object {
        /**
         * 새로운 주문 상품을 생성합니다.
         */
        fun create(
            orderItemId: String,
            orderId: String,
            productId: String,
            amount: Long,
            unitPrice: Long
        ): OrderItem {
            if (productId.isBlank()) {
                throw OrderException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            }
            if (amount <= 0) {
                throw OrderException.AmountShouldMoreThan0("주문 수량은 0보다 커야합니다.")
            }
            if (unitPrice <= 0) {
                throw OrderException.AmountShouldMoreThan0("단가는 0보다 커야합니다.")
            }
            
            val totalPrice = amount * unitPrice
            
            return OrderItem(
                orderItemId = orderItemId,
                orderId = orderId,
                productId = productId,
                amount = amount,
                unitPrice = unitPrice,
                totalPrice = totalPrice
            )
        }
    }
} 