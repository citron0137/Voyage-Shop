package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.userpoint.UserPoint
import kr.hhplus.be.server.domain.userpoint.UserPointRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 UserPointRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeUserPointRepository : UserPointRepository {
    
    private val store = ConcurrentHashMap<String, UserPoint>()
    
    override fun create(userPoint: UserPoint): UserPoint {
        store[userPoint.userId] = userPoint
        return userPoint
    }
    
    override fun findByUserId(userId: String): UserPoint? {
        return store[userId]
    }
    
    override fun save(userPoint: UserPoint): UserPoint {
        store[userPoint.userId] = userPoint
        return userPoint
    }
} 