package kr.hhplus.be.server.controller.couponuser

import kr.hhplus.be.server.controller.couponuser.response.*
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

@RestController()
class CouponUserController {
    @GetMapping("/coupon-users")
    fun getAllCouponUsers(): BaseResponse<List<CouponUserResponseDTO>>{
        return BaseResponse.success(listOf(
            CouponUserResponseDTO(
                id = "id1",
                userId = "userId1",
                type = CouponUserResponseDTOType.DISCOUNT_FIXED_AMOUNT,
                fixedDiscountAmount = 0,
                discountPercentage = null,
            ),
            CouponUserResponseDTO(
                id = "id2",
                userId = "userId1",
                type = CouponUserResponseDTOType.DISCOUNT_PERCENTAGE,
                discountPercentage = 0,
                fixedDiscountAmount = null,
            ),
        ) )
    }

}