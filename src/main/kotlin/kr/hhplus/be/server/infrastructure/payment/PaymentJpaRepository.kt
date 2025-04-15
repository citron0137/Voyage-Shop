package kr.hhplus.be.server.infrastructure.payment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * PaymentEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface PaymentJpaRepository : JpaRepository<PaymentEntity, String> {
    /**
     * 사용자 ID로 결제 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 결제 정보 목록
     */
    fun findByUserId(userId: String): List<PaymentEntity>
} 