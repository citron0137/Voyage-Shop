package kr.hhplus.be.server.domain.user

import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.uuid.Uuid

@Service
class UserService (
    private val userRepository: UserRepository
){
    // Create
    fun createUser():User{
        val user = User(userId = UUID.randomUUID().toString())
        userRepository.create(user)
        return user
    }

    // Read
    fun findUserById(userId:String):User?{
        return userRepository.findById(userId)
    }
    
    /**
     * 사용자를 ID로 조회하고, 없으면 예외를 발생시킵니다.
     * 
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     */
    fun findUserByIdOrThrow(userId: String): User {
        return findUserById(userId) ?: throw UserException.NotFound("userId($userId)로 User를 찾을 수 없습니다.")
    }
    
    fun getAllUsers(): List<User>{
        return userRepository.findAll()
    }
}