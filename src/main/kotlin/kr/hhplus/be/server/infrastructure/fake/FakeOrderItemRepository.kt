package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderItemRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 OrderItemRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeOrderItemRepository : OrderItemRepository {
    
    private val store = ConcurrentHashMap<String, OrderItem>()
    
    override fun create(orderItem: OrderItem): OrderItem {
        store[orderItem.orderItemId] = orderItem
        return orderItem
    }
    
    override fun createAll(orderItems: List<OrderItem>): List<OrderItem> {
        orderItems.forEach { orderItem ->
            store[orderItem.orderItemId] = orderItem
        }
        return orderItems
    }
    
    override fun findById(orderItemId: String): OrderItem? {
        return store[orderItemId]
    }
    
    override fun findByOrderId(orderId: String): List<OrderItem> {
        return store.values.filter { it.orderId == orderId }
    }
} 