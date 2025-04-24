package kr.hhplus.be.server.controller.couponuser

import kr.hhplus.be.server.application.couponuser.CouponUserFacade
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import org.springframework.web.bind.annotation.*

/**
 * 쿠폰 사용자 컨트롤러
 */
@RestController
class CouponUserController(
    private val couponUserFacade: CouponUserFacade
) : CouponUserControllerApi {
    /**
     * 모든 쿠폰 사용자를 조회합니다.
     *
     * @return 쿠폰 사용자 목록 응답
     */
    override fun getAllCouponUsers(): BaseResponse<List<CouponUserResponse.Single>> {
        val result = couponUserFacade.getAllCoupons()
        
        val responseList = result.couponUsers.map { couponUserResult ->
            val type = when (couponUserResult.benefitMethod) {
                CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT -> CouponUserResponse.Type.DISCOUNT_FIXED_AMOUNT
                CouponUserBenefitMethod.DISCOUNT_PERCENTAGE -> CouponUserResponse.Type.DISCOUNT_PERCENTAGE
            }
            
            val fixedDiscountAmount = if (couponUserResult.benefitMethod == CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT) {
                couponUserResult.benefitAmount.toLong()
            } else {
                null
            }
            
            val discountPercentage = if (couponUserResult.benefitMethod == CouponUserBenefitMethod.DISCOUNT_PERCENTAGE) {
                couponUserResult.benefitAmount.toInt()
            } else {
                null
            }
            
            CouponUserResponse.Single(
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