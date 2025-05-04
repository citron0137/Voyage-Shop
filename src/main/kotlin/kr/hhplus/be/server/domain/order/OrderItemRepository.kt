package kr.hhplus.be.server.domain.order

interface OrderItemRepository {
    fun create(orderItem: OrderItem): OrderItem
    fun createAll(orderItems: List<OrderItem>): List<OrderItem>
    fun findById(orderItemId: String): OrderItem?
    fun findByOrderId(orderId: String): List<OrderItem>
    fun findByOrderIdIn(orderIds: List<String>): List<OrderItem>
} 