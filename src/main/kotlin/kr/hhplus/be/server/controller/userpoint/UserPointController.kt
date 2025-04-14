package kr.hhplus.be.server.controller.userpoint

import kr.hhplus.be.server.application.userpoint.UserPointCriteria
import kr.hhplus.be.server.application.userpoint.UserPointFacade
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 사용자 포인트 컨트롤러
 */
@RestController
class UserPointController(
    private val userPointFacade: UserPointFacade
) : UserPointControllerApi {
    /**
     * 사용자의 포인트 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 포인트 정보
     */
    override fun getUserPoint(@PathVariable userId: String): ResponseEntity<UserPointResponse.Single> {
        val criteria = UserPointCriteria.GetByUserId(userId)
        val result = userPointFacade.getUserPoint(criteria)
        return ResponseEntity.ok(UserPointResponse.Single.from(result))
    }

    /**
     * 사용자의 포인트를 충전합니다.
     *
     * @param userId 사용자 ID
     * @param request 포인트 충전 요청
     * @return 충전 후 사용자 포인트 정보
     */
    override fun chargePoint(
        @PathVariable userId: String,
        @RequestBody request: UserPointRequest.Charge
    ): ResponseEntity<UserPointResponse.Single> {
        val criteria = UserPointCriteria.Charge(userId, request.amount)
        val result = userPointFacade.chargePoint(criteria)
        return ResponseEntity.ok(UserPointResponse.Single.from(result))
    }
} 