package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 OrderRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeOrderRepository : OrderRepository {
    
    private val store = ConcurrentHashMap<String, Order>()
    
    override fun create(order: Order): Order {
        store[order.orderId] = order
        return order
    }
    
    override fun findById(orderId: String): Order? {
        return store[orderId]
    }
    
    override fun findByUserId(userId: String): List<Order> {
        return store.values.filter { it.userId == userId }
    }
    
    override fun findAll(): List<Order> {
        return store.values.toList()
    }
} 