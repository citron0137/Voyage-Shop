package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

interface OrderRepository {
    fun create(order: Order): Order
    fun findById(orderId: String): Order?
    fun findByUserId(userId: String): List<Order>
    fun findAll(): List<Order>
    fun findByCreatedAtAfter(startDate: LocalDateTime): List<Order>
} 