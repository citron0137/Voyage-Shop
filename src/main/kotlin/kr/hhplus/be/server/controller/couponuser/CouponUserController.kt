package kr.hhplus.be.server.controller.couponuser

import kr.hhplus.be.server.application.couponuser.CouponUserFacade
import kr.hhplus.be.server.controller.couponuser.response.*
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import org.springframework.web.bind.annotation.*

@RestController()
class CouponUserController(
    private val couponUserFacade: CouponUserFacade
) {
    @GetMapping("/coupon-users")
    fun getAllCouponUsers(): BaseResponse<List<CouponUserResponseDTO>> {
        val result = couponUserFacade.getAllCoupons()
        
        val responseList = result.couponUsers.map { couponUserResult ->
            val type = when (couponUserResult.benefitMethod) {
                CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT -> CouponUserResponseDTOType.DISCOUNT_FIXED_AMOUNT
                CouponBenefitMethod.DISCOUNT_PERCENTAGE -> CouponUserResponseDTOType.DISCOUNT_PERCENTAGE
            }
            
            val fixedDiscountAmount = if (couponUserResult.benefitMethod == CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT) {
                couponUserResult.benefitAmount.toLong()
            } else {
                null
            }
            
            val discountPercentage = if (couponUserResult.benefitMethod == CouponBenefitMethod.DISCOUNT_PERCENTAGE) {
                couponUserResult.benefitAmount.toInt()
            } else {
                null
            }
            
            CouponUserResponseDTO(
                id = couponUserResult.couponUserId,
                userId = couponUserResult.userId,
                type = type,
                discountPercentage = discountPercentage,
                fixedDiscountAmount = fixedDiscountAmount
            )
        }
        
        return BaseResponse.success(responseList)
    }
}