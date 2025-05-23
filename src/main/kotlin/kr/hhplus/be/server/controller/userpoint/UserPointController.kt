package kr.hhplus.be.server.controller.userpoint

import kr.hhplus.be.server.application.userpoint.UserPointCriteria
import kr.hhplus.be.server.application.userpoint.UserPointApplication
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

/**
 * 사용자 포인트 컨트롤러
 */
@RestController
class UserPointController(
    private val userPointApplication: UserPointApplication
) : UserPointControllerApi {
    /**
     * 사용자의 포인트 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 포인트 정보
     */
    override fun getUserPoint(@PathVariable userId: String): BaseResponse<UserPointResponse.Single> {
        val criteria = UserPointCriteria.GetByUserId(userId)
        val result = userPointApplication.getUserPoint(criteria)
        return BaseResponse.success(UserPointResponse.Single.from(result))
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
    ): BaseResponse<UserPointResponse.Single> {
        val criteria = UserPointCriteria.Charge(userId, request.amount)
        val result = userPointApplication.chargePoint(criteria)
        return BaseResponse.success(UserPointResponse.Single.from(result))
    }
} 