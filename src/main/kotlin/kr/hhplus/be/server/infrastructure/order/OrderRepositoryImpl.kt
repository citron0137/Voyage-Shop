package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * OrderRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class OrderRepositoryImpl(private val orderJpaRepository: OrderJpaRepository) : OrderRepository {
    
    @Transactional
    override fun create(order: Order): Order {
        val orderEntity = OrderJpaEntity.fromDomain(order)
        return orderJpaRepository.save(orderEntity).toDomain()
    }
    
    @Transactional(readOnly = true)
    override fun findById(orderId: String): Order? {
        return orderJpaRepository.findByIdOrNull(orderId)?.toDomain()
    }
    
    @Transactional(readOnly = true)
    override fun findByUserId(userId: String): List<Order> {
        return orderJpaRepository.findByUserId(userId).map { it.toDomain() }
    }
    
    @Transactional(readOnly = true)
    override fun findAll(): List<Order> {
        return orderJpaRepository.findAll().map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findByCreatedAtAfter(startDate: LocalDateTime): List<Order> {
        return orderJpaRepository.findByCreatedAtAfter(startDate).map { it.toDomain() }
    }
} 