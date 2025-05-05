package kr.hhplus.be.server.controller.couponevent

import kr.hhplus.be.server.application.couponevent.CouponEventApplication
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

/**
 * 쿠폰 이벤트 컨트롤러
 */
@RestController
class CouponEventController(
    private val couponEventApplication: CouponEventApplication
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
        val criteria = req.toCriteria()
        val result = couponEventApplication.createCouponEvent(criteria)
        return BaseResponse.success(CouponEventResponse.Event.from(result))
    }

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     *
     * @return 쿠폰 이벤트 목록
     */
    override fun getAllCouponEvents(): BaseResponse<List<CouponEventResponse.Event>> {
        val result = couponEventApplication.getAllCouponEvents()
        return BaseResponse.success(CouponEventResponse.List.from(result).items)
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
        val criteria = req.toCriteria(couponEventId)
        val result = couponEventApplication.issueCouponUser(criteria)
        return BaseResponse.success(CouponEventResponse.IssueCoupon.from(result))
    }
}