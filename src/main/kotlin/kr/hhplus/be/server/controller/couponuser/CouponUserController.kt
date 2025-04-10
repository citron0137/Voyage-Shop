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
        // 현재 컨트롤러는 userId를 하드코딩하고 있으므로, 실제 구현에서는 이 부분을 개선해야 할 수 있습니다.
        // 예: 인증된 사용자의 ID를 사용하거나, 쿼리 파라미터로 받는 등
        val userId = "userId1"
        
        val result = couponUserFacade.getAllCouponsByUserId(userId)
        
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