package kr.hhplus.be.server.infrastructure.userpoint

import kr.hhplus.be.server.domain.userpoint.UserPoint
import kr.hhplus.be.server.domain.userpoint.UserPointRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * UserPointRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class UserPointRepositoryImpl(private val userPointJpaRepository: UserPointJpaRepository) : UserPointRepository {
    
    override fun create(userPoint: UserPoint): UserPoint {
        val userPointEntity = UserPointJpaEntity.fromDomain(userPoint)
        return userPointJpaRepository.save(userPointEntity).toDomain()
    }
    
    override fun findByUserId(userId: String): UserPoint? {
        return userPointJpaRepository.findByUserId(userId)?.toDomain()
    }
    
    override fun findByUserIdWithLock(userId: String): UserPoint? {
        return userPointJpaRepository.findByUserIdWithLock(userId)?.toDomain()
    }
    
    override fun save(userPoint: UserPoint): UserPoint {
        val userPointEntity = UserPointJpaEntity.fromDomain(userPoint)
        return userPointJpaRepository.save(userPointEntity).toDomain()
    }
} 