package kr.hhplus.be.server.domain.user

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService (
    private val userRepository: UserRepository
){
    /**
     * 사용자 생성 명령을 처리합니다.
     * 
     * @param command 사용자 생성 명령
     * @return 생성된 사용자
     */
    fun createUser(command: UserCommand.Create): User {
        val user = User(userId = UUID.randomUUID().toString())
        return userRepository.create(user)
    }
    
    /**
     * ID로 사용자 조회 쿼리를 처리합니다.
     * 
     * @param query ID로 사용자 조회 쿼리
     * @return 조회된 사용자(없으면 null)
     */
    fun findUserById(query: UserQuery.GetById): User? {
        return userRepository.findById(query.userId)
    }
    

    /**
     * 사용자를 ID로 조회하고, 없으면 예외를 발생시킵니다.
     * 
     * @param query ID로 사용자 조회 쿼리
     * @return 조회된 사용자
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     */
    fun getUserById(query: UserQuery.GetById): User {
        return userRepository.findById(query.userId) 
            ?: throw UserException.NotFound("userId(${query.userId})로 User를 찾을 수 없습니다.")
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}