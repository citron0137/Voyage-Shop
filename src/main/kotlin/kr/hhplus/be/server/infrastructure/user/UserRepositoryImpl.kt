package kr.hhplus.be.server.infrastructure.user

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * UserRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    /**
     * 사용자를 생성합니다.
     *
     * @param user 생성할 사용자 정보
     * @return 생성된 사용자 정보
     */
    @Transactional
    override fun create(user: User): User {
        val userEntity = UserEntity.of(user)
        val savedEntity = userJpaRepository.save(userEntity)
        return UserEntity.toDomain(savedEntity)
    }

    /**
     * ID로 사용자를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자 정보 또는 null
     */
    @Transactional(readOnly = true)
    override fun findById(userId: String): User? {
        return userJpaRepository.findByIdOrNull(userId)?.let { UserEntity.toDomain(it) }
    }

    /**
     * 모든 사용자를 조회합니다.
     *
     * @return 모든 사용자 정보 목록
     */
    @Transactional(readOnly = true)
    override fun findAll(): List<User> {
        return userJpaRepository.findAll().map { UserEntity.toDomain(it) }
    }
} 