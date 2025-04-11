package kr.hhplus.be.server.controller.couponevent

import kr.hhplus.be.server.application.couponevent.CouponEventFacade
import kr.hhplus.be.server.application.couponevent.dto.CreateCouponEventCriteria
import kr.hhplus.be.server.application.couponevent.dto.IssueCouponCriteria
import kr.hhplus.be.server.controller.couponevent.request.CreateCouponEventRequest
import kr.hhplus.be.server.controller.couponevent.request.IssueCouponEventCouponUserRequest
import kr.hhplus.be.server.controller.couponevent.response.CouponEventIssueCouponResponseDTO
import kr.hhplus.be.server.controller.couponevent.response.CouponEventResponseDTO
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

@RestController()
class CouponEventController(
    private val couponEventFacade: CouponEventFacade
) {
    @PostMapping("/coupon-events")
    fun createCouponEvent(
        @RequestBody req: CreateCouponEventRequest
    ): BaseResponse<CouponEventResponseDTO>{
        val criteria = CreateCouponEventCriteria(
            benefitMethod = req.benefitMethod,
            benefitAmount = req.benefitAmount,
            totalIssueAmount = req.totalIssueAmount
        )
        
        val result = couponEventFacade.createCouponEvent(criteria)
        
        return BaseResponse.success(
            CouponEventResponseDTO(
                id = result.id,
                benefitMethod = result.benefitMethod,
                benefitAmount = result.benefitAmount,
                totalIssueAmount = result.totalIssueAmount,
                leftIssueAmount = result.leftIssueAmount
            )
        )
    }

    @GetMapping("/coupon-events")
    fun getAllCouponEvents(): BaseResponse<List<CouponEventResponseDTO>>{
        val couponEvents = couponEventFacade.getAllCouponEvents()
        
        val responseList = couponEvents.map {
            CouponEventResponseDTO(
                id = it.id,
                benefitMethod = it.benefitMethod,
                benefitAmount = it.benefitAmount,
                totalIssueAmount = it.totalIssueAmount,
                leftIssueAmount = it.leftIssueAmount
            )
        }
        
        return BaseResponse.success(responseList)
    }

    @PostMapping("/coupon-events/{couponEventId}/issue-coupon-user")
    fun issueCouponUser(
        @PathVariable couponEventId: String,
        @RequestBody req: IssueCouponEventCouponUserRequest
    ): BaseResponse<CouponEventIssueCouponResponseDTO>{
        val criteria = IssueCouponCriteria(userId = req.userId)
        
        val result = couponEventFacade.issueCouponUser(couponEventId, criteria)
        
        return BaseResponse.success(
            CouponEventIssueCouponResponseDTO(
                couponUserId = result.couponUserId
            )
        )
    }
}