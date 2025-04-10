package kr.hhplus.be.server.controller.user

import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.application.user.UserResult
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.controller.user.response.UserResponseDTO
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자 API 구현체
 */
@RestController
class UserController(
    private val userFacade: UserFacade
) : UserControllerApi {
    
    /**
     * 새로운 사용자를 생성합니다.
     *
     * @return 생성된 사용자 정보
     */
    override fun createUser(): BaseResponse<UserResponseDTO> {
        val result = userFacade.createUser()
        return BaseResponse.success(convertToResponseDto(result))
    }
    
    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자 정보
     */
    override fun getUserById(userId: String): BaseResponse<UserResponseDTO> {
        val result = userFacade.findUserById(userId)
        return BaseResponse.success(convertToResponseDto(result))
    }
    
    /**
     * 모든 사용자 목록을 조회합니다.
     *
     * @return 사용자 목록
     */
    override fun getAllUsers(): BaseResponse<List<UserResponseDTO>> {
        val result = userFacade.getAllUsers()
        val dtos = result.users.map { convertToResponseDto(it) }
        
        return BaseResponse.success(dtos)
    }
    
    /**
     * UserResult를 UserResponseDTO로 변환합니다.
     */
    private fun convertToResponseDto(result: UserResult): UserResponseDTO {
        return UserResponseDTO(
            id = result.userId,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt
        )
    }
}