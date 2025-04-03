package kr.hhplus.be.server.controller.couponevent

import kr.hhplus.be.server.controller.couponevent.request.CreateCouponEventRequest
import kr.hhplus.be.server.controller.couponevent.request.IssueCouponEventCouponUserRequest
import kr.hhplus.be.server.controller.couponevent.response.CouponEventIssueCouponResponseDTO
import kr.hhplus.be.server.controller.couponevent.response.CouponEventResponseDTO
import kr.hhplus.be.server.controller.couponevent.response.CouponEventResponseDTOCouponType
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

@RestController()
class CouponEventController {
    @PostMapping("/coupon-events")
    fun createCouponEvent(
        @RequestBody req: CreateCouponEventRequest
    ): BaseResponse<CouponEventResponseDTO>{
        return BaseResponse.success(
            CouponEventResponseDTO(
                id = "id1",
                couponType = CouponEventResponseDTOCouponType.DISCOUNT_FIXED_AMOUNT,
                fixedDiscountAmount = 0,
                discountPercentage = null,
                initialStock = 0,
                currentStock = 0,
            ),
        )
    }

    @GetMapping("/coupon-events")
    fun getAllCouponEvents(): BaseResponse<List<CouponEventResponseDTO>>{
        return BaseResponse.success(listOf(
            CouponEventResponseDTO(
                id = "id1",
                couponType = CouponEventResponseDTOCouponType.DISCOUNT_FIXED_AMOUNT,
                fixedDiscountAmount = 0,
                discountPercentage = null,
                initialStock = 0,
                currentStock = 0,
            ),
            CouponEventResponseDTO(
                id = "id2",
                couponType = CouponEventResponseDTOCouponType.DISCOUNT_PERCENTAGE,
                discountPercentage = 0,
                fixedDiscountAmount = null,
                initialStock = 0,
                currentStock = 0,
            ),
        ) )
    }

    @PostMapping("/coupon-events/{couponEventId}/issue-coupon-user")
    fun issueCouponUser(
        @PathVariable couponEventId: String,
        @RequestBody req: IssueCouponEventCouponUserRequest
    ): BaseResponse<CouponEventIssueCouponResponseDTO>{
        return BaseResponse.success(CouponEventIssueCouponResponseDTO(
            couponUserId = "couponUserId1",
        ))
    }



}