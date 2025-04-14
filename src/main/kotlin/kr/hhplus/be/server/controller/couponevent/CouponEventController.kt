package kr.hhplus.be.server.controller.couponevent

import kr.hhplus.be.server.application.couponevent.CouponEventCriteria
import kr.hhplus.be.server.application.couponevent.CouponEventFacade
import kr.hhplus.be.server.application.couponevent.CouponEventResult
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

/**
 * 쿠폰 이벤트 컨트롤러
 */
@RestController
class CouponEventController(
    private val couponEventFacade: CouponEventFacade
) : CouponEventControllerApi {
    /**
     * 쿠폰 이벤트를 생성합니다.
     *
     * @param req 쿠폰 이벤트 생성 요청
     * @return 생성된 쿠폰 이벤트 정보
     */
    override fun createCouponEvent(
        req: CouponEventRequest.Create
    ): BaseResponse<CouponEventResponse.Event> {
        val criteria = CouponEventCriteria.Create(
            benefitMethod = req.benefitMethod,
            benefitAmount = req.benefitAmount,
            totalIssueAmount = req.totalIssueAmount.toLong()
        )
        
        val result = couponEventFacade.createCouponEvent(criteria)
        
        return BaseResponse.success(
            CouponEventResponse.Event(
                id = result.id,
                benefitMethod = result.benefitMethod.name,
                benefitAmount = result.benefitAmount,
                totalIssueAmount = result.totalIssueAmount,
                leftIssueAmount = result.leftIssueAmount
            )
        )
    }

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     *
     * @return 쿠폰 이벤트 목록
     */
    override fun getAllCouponEvents(): BaseResponse<List<CouponEventResponse.Event>> {
        val result = couponEventFacade.getAllCouponEvents()
        
        val responseList = result.couponEvents.map { event ->
            CouponEventResponse.Event(
                id = event.id,
                benefitMethod = event.benefitMethod.name,
                benefitAmount = event.benefitAmount,
                totalIssueAmount = event.totalIssueAmount,
                leftIssueAmount = event.leftIssueAmount
            )
        }
        
        return BaseResponse.success(responseList)
    }

    /**
     * 사용자에게 쿠폰을 발급합니다.
     *
     * @param couponEventId 쿠폰 이벤트 ID
     * @param req 쿠폰 발급 요청
     * @return 발급된 쿠폰 정보
     */
    override fun issueCouponUser(
        couponEventId: String,
        req: CouponEventRequest.IssueCoupon
    ): BaseResponse<CouponEventResponse.IssueCoupon> {
        val criteria = CouponEventCriteria.IssueCoupon(
            couponEventId = couponEventId,
            userId = req.userId
        )
        
        val result = couponEventFacade.issueCouponUser(criteria)
        
        return BaseResponse.success(
            CouponEventResponse.IssueCoupon(
                couponUserId = result.couponUserId
            )
        )
    }
}