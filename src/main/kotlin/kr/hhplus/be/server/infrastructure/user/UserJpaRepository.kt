package kr.hhplus.be.server.infrastructure.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * UserJpaEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface UserJpaRepository : JpaRepository<UserJpaEntity, String> {
    // 기본 CRUD 작업은 JpaRepository에서 제공됩니다.
    // 필요한 경우 여기에 추가 쿼리 메서드를 정의할 수 있습니다.
} 