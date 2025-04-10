package kr.hhplus.be.server.infrastructure.userpoint

import kr.hhplus.be.server.domain.userpoint.UserPoint
import kr.hhplus.be.server.domain.userpoint.UserPointRepository
import org.springframework.stereotype.Repository

@Repository
class UserPointRepositoryImpl: UserPointRepository {
    override fun create(userPoint: UserPoint): UserPoint {
        TODO("Not yet implemented")
    }

    override fun findByUserId(userId: String): UserPoint? {
        TODO("Not yet implemented")
    }

    override fun save(userPoint: UserPoint): UserPoint {
        TODO("Not yet implemented")
    }
}