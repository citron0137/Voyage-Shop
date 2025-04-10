package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.userpoint.UserPointCommand
import kr.hhplus.be.server.domain.userpoint.UserPointException
import kr.hhplus.be.server.domain.userpoint.UserPointService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 관련 서비스를 조합하는 파사드 클래스
 */
@Component
class UserFacade(
    private val userService: UserService,
    private val userPointService: UserPointService
) {
    /**
     * 새로운 사용자를 생성합니다.
     * 사용자 생성과 함께 해당 사용자의 포인트 정보도 함께 생성합니다.
     *
     * @return 생성된 사용자 정보
     * @throws UserPointException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws RuntimeException 사용자 생성 또는 포인트 생성 과정에서 예기치 않은 오류가 발생한 경우
     */
    @Transactional
    fun createUser(): UserResult {
        // 사용자 생성
        val createdUser = userService.createUser()
        
        // 사용자 포인트 생성
        val createPointCommand = UserPointCommand.Create(userId = createdUser.userId)
        userPointService.create(createPointCommand)
        
        return UserResult.from(createdUser)
    }

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우 (U_NOT_FOUND)
     */
    fun findUserById(userId: String): UserResult {
        val user = userService.findUserByIdOrThrow(userId)
        return UserResult.from(user)
    }

    /**
     * 모든 사용자 목록을 조회합니다.
     *
     * @return 사용자 목록
     * @throws RuntimeException 사용자 목록 조회 과정에서 예기치 않은 오류가 발생한 경우
     */
    fun getAllUsers(): UserListResult {
        val users = userService.getAllUsers()
        return UserListResult.from(users)
    }
}
