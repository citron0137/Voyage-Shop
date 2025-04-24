package kr.hhplus.be.server.infrastructure.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * OrderJpaEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface OrderJpaRepository : JpaRepository<OrderJpaEntity, String> {
    /**
     * 사용자 ID로 주문을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 주문 목록
     */
    fun findByUserId(userId: String): List<OrderJpaEntity>
} 