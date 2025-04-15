package kr.hhplus.be.server.infrastructure.couponevent

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * CouponEventEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface CouponEventJpaRepository : JpaRepository<CouponEventEntity, String> {
    /**
     * ID로 쿠폰 이벤트를 조회하며, 비관적 쓰기 락을 사용합니다.
     * 재고 차감 시 동시성 문제를 방지하기 위해 사용됩니다.
     *
     * @param id 쿠폰 이벤트 ID
     * @return 해당 ID의 쿠폰 이벤트 엔티티 또는 null
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEventEntity c WHERE c.id = :id")
    fun findByIdWithLock(id: String): CouponEventEntity?
} 