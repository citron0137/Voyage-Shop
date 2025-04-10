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
    fun getAllUsers(): List<User>{
        return userRepository.findAll()
    }
}