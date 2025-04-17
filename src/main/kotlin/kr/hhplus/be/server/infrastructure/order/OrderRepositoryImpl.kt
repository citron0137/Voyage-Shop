package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * OrderRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class OrderRepositoryImpl(private val orderJpaRepository: OrderJpaRepository) : OrderRepository {
    
    override fun create(order: Order): Order {
        val orderEntity = OrderEntity.from(order)
        return orderJpaRepository.save(orderEntity).toOrder()
    }
    
    override fun findById(orderId: String): Order? {
        return orderJpaRepository.findByIdOrNull(orderId)?.toOrder()
    }
    
    override fun findByUserId(userId: String): List<Order> {
        return orderJpaRepository.findByUserId(userId).map { it.toOrder() }
    }
    
    override fun findAll(): List<Order> {
        return orderJpaRepository.findAll().map { it.toOrder() }
    }
} 