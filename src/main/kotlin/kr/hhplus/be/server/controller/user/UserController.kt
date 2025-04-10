package kr.hhplus.be.server.controller.user

import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.controller.user.response.UserResponseDTO
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController()
class UserController {
    @PostMapping("/users")
    fun createUser(): BaseResponse<UserResponseDTO>{
        val now = LocalDateTime.now()
        return BaseResponse.success(UserResponseDTO(id = "id1", createdAt = now, updatedAt = now))
    }
}