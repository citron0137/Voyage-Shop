package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUserCommand
import kr.hhplus.be.server.domain.couponevent.BenefitMethod
import kr.hhplus.be.server.domain.couponevent.CEInvalidBenefitMethodException
import kr.hhplus.be.server.domain.couponevent.CreateCouponEventCommand

/**
 * 쿠폰 이벤트 관련 요청 기준을 담는 클래스
 */
class CouponEventCriteria {
    /**
     * 모든 쿠폰 이벤트 조회 요청
     */
    class GetAll
    
    /**
     * 쿠폰 이벤트 ID로 조회 요청
     */
    data class GetById(
        val couponEventId: String
    )
    
    /**
     * 쿠폰 이벤트 생성 요청
     */
    data class Create(
        val benefitMethod: String,
        val benefitAmount: String,
        val totalIssueAmount: Long
    ) {
        /**
         * 도메인 Command로 변환
         */
        fun toCommand(): CreateCouponEventCommand {
            // 문자열을 BenefitMethod 열거형으로 변환
            val benefitMethodEnum = when(benefitMethod) {
                "DISCOUNT_FIXED_AMOUNT" -> BenefitMethod.DISCOUNT_FIXED_AMOUNT
                "DISCOUNT_PERCENTAGE" -> BenefitMethod.DISCOUNT_PERCENTAGE
                else -> throw CEInvalidBenefitMethodException(benefitMethod)
            }
            
            return CreateCouponEventCommand(
                benefitMethod = benefitMethodEnum,
                benefitAmount = benefitAmount,
                totalIssueAmount = totalIssueAmount
            )
        }
    }
    
    /**
     * 쿠폰 발급 요청
     */
    data class IssueCoupon(
        val couponEventId: String,
        val userId: String
    ) {
        /**
         * CouponUserCommand로 변환
         * 
         * @param benefitMethod 쿠폰 이벤트의 혜택 방식
         * @param benefitAmount 혜택 금액
         * @return 생성된 CouponUserCommand.Create 객체
         */
        fun toCommand(benefitMethod: BenefitMethod, benefitAmount: String): CouponUserCommand.Create {
            // BenefitMethod를 CouponBenefitMethod로 변환
            val couponBenefitMethod = when (benefitMethod) {
                BenefitMethod.DISCOUNT_FIXED_AMOUNT -> CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT
                BenefitMethod.DISCOUNT_PERCENTAGE -> CouponBenefitMethod.DISCOUNT_PERCENTAGE
            }
            
            return CouponUserCommand.Create(
                userId = userId,
                benefitMethod = couponBenefitMethod,
                benefitAmount = benefitAmount
            )
        }
    }
} 