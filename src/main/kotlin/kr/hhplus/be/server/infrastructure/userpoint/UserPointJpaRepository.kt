package kr.hhplus.be.server.infrastructure.userpoint

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * UserPointEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface UserPointJpaRepository : JpaRepository<UserPointEntity, String> {
    /**
     * 사용자 ID로 사용자 포인트 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 포인트 정보
     */
    fun findByUserId(userId: String): UserPointEntity?
} 