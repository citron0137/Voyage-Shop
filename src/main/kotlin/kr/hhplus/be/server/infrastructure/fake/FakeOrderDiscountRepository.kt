package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.order.OrderDiscount
import kr.hhplus.be.server.domain.order.OrderDiscountRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 OrderDiscountRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeOrderDiscountRepository : OrderDiscountRepository {
    
    private val store = ConcurrentHashMap<String, OrderDiscount>()
    
    override fun create(orderDiscount: OrderDiscount): OrderDiscount {
        store[orderDiscount.orderDiscountId] = orderDiscount
        return orderDiscount
    }
    
    override fun createAll(orderDiscounts: List<OrderDiscount>): List<OrderDiscount> {
        orderDiscounts.forEach { orderDiscount ->
            store[orderDiscount.orderDiscountId] = orderDiscount
        }
        return orderDiscounts
    }
    
    override fun findById(orderDiscountId: String): OrderDiscount? {
        return store[orderDiscountId]
    }
    
    override fun findByOrderId(orderId: String): List<OrderDiscount> {
        return store.values.filter { it.orderId == orderId }
    }
} 