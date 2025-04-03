package kr.hhplus.be.server.controller.user

import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.controller.user.response.UserResponseDTO
import org.springframework.web.bind.annotation.*

@RestController()
class UserController {
    @PostMapping("/users")
    fun createUser(): BaseResponse<UserResponseDTO>{
        return BaseResponse.success(UserResponseDTO(id = "id1"))
    }
}