package kr.hhplus.be.server.infrastructure.user

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl : UserRepository {
    override fun create(user: User): User {
        TODO("Not yet implemented")
    }
    override fun findById(userId: String): User? {
        TODO("Not yet implemented")
    }
    override fun findAll(): List<User> {
        TODO("Not yet implemented")
    }

}