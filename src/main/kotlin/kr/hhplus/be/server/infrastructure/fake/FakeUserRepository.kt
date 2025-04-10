package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 UserRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeUserRepository : UserRepository {
    
    private val store = ConcurrentHashMap<String, User>()
    
    override fun create(user: User): User {
        store[user.userId] = user
        return user
    }
    
    override fun findById(userId: String): User? {
        return store[userId]
    }
    
    override fun findAll(): List<User> {
        return store.values.toList()
    }
} 