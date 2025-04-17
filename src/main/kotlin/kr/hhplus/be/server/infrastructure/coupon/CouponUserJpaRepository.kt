package kr.hhplus.be.server.infrastructure.coupon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * CouponUserEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface CouponUserJpaRepository : JpaRepository<CouponUserEntity, String> {
    /**
     * 사용자 ID로 쿠폰 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 쿠폰 사용자 정보 목록
     */
    fun findByUserId(userId: String): List<CouponUserEntity>
} 