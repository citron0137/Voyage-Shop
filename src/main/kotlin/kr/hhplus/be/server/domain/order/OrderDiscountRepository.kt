package kr.hhplus.be.server.domain.order

interface OrderDiscountRepository {
    fun create(orderDiscount: OrderDiscount): OrderDiscount
    fun createAll(orderDiscounts: List<OrderDiscount>): List<OrderDiscount>
    fun findById(orderDiscountId: String): OrderDiscount?
    fun findByOrderId(orderId: String): List<OrderDiscount>
} 