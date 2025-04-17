package kr.hhplus.be.server.infrastructure.userpoint

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import jakarta.persistence.LockModeType

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
    
    /**
     * 사용자 ID로 사용자 포인트 정보를 조회하면서 동시성 제어를 위한 락을 획득합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 포인트 정보 (락 획득)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM UserPointEntity p WHERE p.userId = :userId")
    fun findByUserIdWithLock(userId: String): UserPointEntity?
} 