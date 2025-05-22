package kr.hhplus.be.server.infrastructure.coupon

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * CouponUserJpaEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface CouponUserJpaRepository : JpaRepository<CouponUserJpaEntity, String> {
    /**
     * 사용자 ID로 쿠폰 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 쿠폰 사용자 정보 목록
     */
    fun findByUserId(userId: String): List<CouponUserJpaEntity>

    /**
     * 쿠폰 사용자 ID로 쿠폰 사용자 정보를 조회합니다.
     *
     * @param couponUserId 쿠폰 사용자 ID
     * @return 해당 쿠폰 사용자의 정보
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponUserJpaEntity c WHERE c.couponUserId = :couponUserId")
    fun findByIdWithLock(couponUserId: String): CouponUserJpaEntity?
} 