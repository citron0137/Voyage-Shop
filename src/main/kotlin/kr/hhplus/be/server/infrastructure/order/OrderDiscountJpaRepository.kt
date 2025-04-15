package kr.hhplus.be.server.infrastructure.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * OrderDiscountEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface OrderDiscountJpaRepository : JpaRepository<OrderDiscountEntity, String> {
    /**
     * 주문 ID로 할인 정보를 조회합니다.
     *
     * @param orderId 주문 ID
     * @return 해당 주문의 할인 정보 목록
     */
    fun findByOrderId(orderId: String): List<OrderDiscountEntity>
} 