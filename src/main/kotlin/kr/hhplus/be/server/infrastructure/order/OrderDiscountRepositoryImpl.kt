package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.OrderDiscount
import kr.hhplus.be.server.domain.order.OrderDiscountRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * OrderDiscountRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class OrderDiscountRepositoryImpl(
    private val orderDiscountJpaRepository: OrderDiscountJpaRepository
) : OrderDiscountRepository {

    /**
     * 주문 할인 정보를 생성합니다.
     *
     * @param orderDiscount 생성할 주문 할인 정보
     * @return 생성된 주문 할인 정보
     */
    @Transactional
    override fun create(orderDiscount: OrderDiscount): OrderDiscount {
        val entity = OrderDiscountEntity.of(orderDiscount)
        val savedEntity = orderDiscountJpaRepository.save(entity)
        return OrderDiscountEntity.toDomain(savedEntity)
    }

    /**
     * 여러 주문 할인 정보를 한 번에 생성합니다.
     *
     * @param orderDiscounts 생성할 주문 할인 정보 목록
     * @return 생성된 주문 할인 정보 목록
     */
    @Transactional
    override fun createAll(orderDiscounts: List<OrderDiscount>): List<OrderDiscount> {
        val entities = orderDiscounts.map { OrderDiscountEntity.of(it) }
        val savedEntities = orderDiscountJpaRepository.saveAll(entities)
        return savedEntities.map { OrderDiscountEntity.toDomain(it) }
    }

    /**
     * ID로 주문 할인 정보를 조회합니다.
     *
     * @param orderDiscountId 조회할 주문 할인 ID
     * @return 조회된 주문 할인 정보 또는 null
     */
    @Transactional(readOnly = true)
    override fun findById(orderDiscountId: String): OrderDiscount? {
        return orderDiscountJpaRepository.findByIdOrNull(orderDiscountId)?.let { OrderDiscountEntity.toDomain(it) }
    }

    /**
     * 주문 ID로 주문 할인 정보를 조회합니다.
     *
     * @param orderId 조회할 주문 ID
     * @return 해당 주문의 할인 정보 목록
     */
    @Transactional(readOnly = true)
    override fun findByOrderId(orderId: String): List<OrderDiscount> {
        return orderDiscountJpaRepository.findByOrderId(orderId).map { OrderDiscountEntity.toDomain(it) }
    }
} 