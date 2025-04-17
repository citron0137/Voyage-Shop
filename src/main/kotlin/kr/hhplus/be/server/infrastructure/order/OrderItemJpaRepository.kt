package kr.hhplus.be.server.infrastructure.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * OrderItemEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface OrderItemJpaRepository : JpaRepository<OrderItemEntity, String> {
    /**
     * 주문 ID로 주문 항목을 조회합니다.
     *
     * @param orderId 주문 ID
     * @return 해당 주문의 주문 항목 목록
     */
    fun findByOrderId(orderId: String): List<OrderItemEntity>
} 