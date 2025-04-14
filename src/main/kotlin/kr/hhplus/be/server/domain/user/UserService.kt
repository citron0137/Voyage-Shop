package kr.hhplus.be.server.domain.user

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService (
    private val userRepository: UserRepository
){
    // Create
    fun createUser(): User {
        val user = User(userId = UUID.randomUUID().toString())
        userRepository.create(user)
        return user
    }

    /**
     * 사용자 생성 명령을 처리합니다.
     * 
     * @param command 사용자 생성 명령
     * @return 생성된 사용자 정보
     */
    fun handle(command: UserCommand.Create): UserInfo.UserDetail {
        val user = createUser()
        return UserInfo.UserDetail.from(user)
    }

    // Read
    fun findUserById(userId: String): User? {
        return userRepository.findById(userId)
    }
    
    /**
     * ID로 사용자 조회 쿼리를 처리합니다.
     * 
     * @param query ID로 사용자 조회 쿼리
     * @return 조회된 사용자 정보(없으면 null)
     */
    fun handle(query: UserQuery.GetById): UserInfo.UserDetail? {
        val user = findUserById(query.userId)
        return user?.let { UserInfo.UserDetail.from(it) }
    }
    
    /**
     * 모든 사용자 조회 쿼리를 처리합니다.
     * 
     * @param query 모든 사용자 조회 쿼리
     * @return 모든 사용자 목록 정보
     */
    fun handle(query: UserQuery.GetAll): UserInfo.UserList {
        val users = getAllUsers()
        return UserInfo.UserList.from(users)
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
    
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}