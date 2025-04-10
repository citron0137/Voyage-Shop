package kr.hhplus.be.server.domain.order

interface OrderRepository {
    fun create(order: Order): Order
    fun findById(orderId: String): Order?
    fun findByUserId(userId: String): List<Order>
    fun findAll(): List<Order>
} 