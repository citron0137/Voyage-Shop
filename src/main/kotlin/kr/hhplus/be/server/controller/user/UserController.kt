package kr.hhplus.be.server.controller.user

import kr.hhplus.be.server.application.user.UserCriteria
import kr.hhplus.be.server.application.user.UserApplication
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자 API 구현체
 */
@RestController
class UserController(
    private val userApplication: UserApplication
) : UserControllerApi {
    
    /**
     * 새로운 사용자를 생성합니다.
     *
     * @return 생성된 사용자 정보
     */
    override fun createUser(): BaseResponse<UserResponse.Single> {
        val result = userApplication.createUser()
        return BaseResponse.success(UserResponse.Single.from(result))
    }
    
    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자 정보
     */
    override fun getUserById(userId: String): BaseResponse<UserResponse.Single> {
        val criteria = UserCriteria.GetById(userId)
        val result = userApplication.findUserById(criteria)
        return BaseResponse.success(UserResponse.Single.from(result))
    }
    
    /**
     * 모든 사용자 목록을 조회합니다.
     *
     * @return 사용자 목록
     */
    override fun getAllUsers(): BaseResponse<UserResponse.List> {
        val result = userApplication.getAllUsers()
        return BaseResponse.success(UserResponse.List.from(result))
    }
}