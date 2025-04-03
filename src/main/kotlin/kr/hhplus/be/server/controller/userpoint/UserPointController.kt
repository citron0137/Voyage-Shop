package kr.hhplus.be.server.controller.userpoint

import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.controller.user.response.UserPointResponseDTO
import kr.hhplus.be.server.controller.userpoint.request.GrantUserPointRequest
import org.springframework.web.bind.annotation.*

@RestController()
class UserPointController {
    // Read
    @GetMapping("/users/{userId}/point")
    fun getUserPoint(
        @PathVariable userId: String
    ): BaseResponse<UserPointResponseDTO>{
        return BaseResponse.success(UserPointResponseDTO(id = "id1", point = 0))
    }

    // Update
    @PostMapping("/users/{userId}/point/grant")
    fun grantUserPoint(
        @PathVariable userId: String,
        @RequestBody req: GrantUserPointRequest,
    ): BaseResponse<UserPointResponseDTO>{
        return BaseResponse.success(UserPointResponseDTO(id = "id1", point = 0))
    }
}