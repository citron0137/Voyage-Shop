package kr.hhplus.be.server.controller.userpoint

import kr.hhplus.be.server.application.userpoint.UserPointFacade
import kr.hhplus.be.server.controller.userpoint.request.UserPointRequestDTO
import kr.hhplus.be.server.controller.userpoint.response.UserPointResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 사용자 포인트 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/user-points")
class UserPointController(
    private val userPointFacade: UserPointFacade
) {
    /**
     * 사용자의 포인트 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 포인트 정보
     */
    @GetMapping("/{userId}")
    fun getUserPoint(@PathVariable userId: String): ResponseEntity<UserPointResponseDTO.Single> {
        val result = userPointFacade.getUserPoint(userId)
        return ResponseEntity.ok(UserPointResponseDTO.Single.from(result))
    }

    /**
     * 사용자의 포인트를 충전합니다.
     *
     * @param userId 사용자 ID
     * @param request 포인트 충전 요청 DTO
     * @return 충전 후 사용자 포인트 정보
     */
    @PostMapping("/{userId}/charge")
    fun chargePoint(
        @PathVariable userId: String,
        @RequestBody request: UserPointRequestDTO.Charge
    ): ResponseEntity<UserPointResponseDTO.Single> {
        val result = userPointFacade.chargePoint(userId, request.amount)
        return ResponseEntity.ok(UserPointResponseDTO.Single.from(result))
    }
} 