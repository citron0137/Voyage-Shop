package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderItemRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * OrderItemRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class OrderItemRepositoryImpl(
    private val orderItemJpaRepository: OrderItemJpaRepository
) : OrderItemRepository {

    /**
     * 주문 항목을 생성합니다.
     *
     * @param orderItem 생성할 주문 항목 정보
     * @return 생성된 주문 항목 정보
     */
    @Transactional
    override fun create(orderItem: OrderItem): OrderItem {
        val entity = OrderItemJpaEntity.fromDomain(orderItem)
        val savedEntity = orderItemJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * 여러 주문 항목을 한 번에 생성합니다.
     *
     * @param orderItems 생성할 주문 항목 목록
     * @return 생성된 주문 항목 목록
     */
    @Transactional
    override fun createAll(orderItems: List<OrderItem>): List<OrderItem> {
        val entities = orderItems.map { OrderItemJpaEntity.fromDomain(it) }
        val savedEntities = orderItemJpaRepository.saveAll(entities)
        return savedEntities.map { it.toDomain() }
    }

    /**
     * ID로 주문 항목을 조회합니다.
     *
     * @param orderItemId 조회할 주문 항목 ID
     * @return 조회된 주문 항목 정보 또는 null
     */
    @Transactional(readOnly = true)
    override fun findById(orderItemId: String): OrderItem? {
        return orderItemJpaRepository.findByIdOrNull(orderItemId)?.toDomain()
    }

    /**
     * 주문 ID로 주문 항목을 조회합니다.
     *
     * @param orderId 조회할 주문 ID
     * @return 해당 주문의 주문 항목 목록
     */
    @Transactional(readOnly = true)
    override fun findByOrderId(orderId: String): List<OrderItem> {
        return orderItemJpaRepository.findByOrderId(orderId).map { it.toDomain() }
    }

    /**
     * 여러 주문 ID로 주문 항목을 조회합니다.
     *
     * @param orderIds 조회할 주문 ID 목록
     * @return 해당 주문 ID의 주문 항목 목록
     */
    @Transactional(readOnly = true)
    override fun findByOrderIdIn(orderIds: List<String>): List<OrderItem> {
        return orderItemJpaRepository.findByOrderIdIn(orderIds).map { it.toDomain() }
    }
} 