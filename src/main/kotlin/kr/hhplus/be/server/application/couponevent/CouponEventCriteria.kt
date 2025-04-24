package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import kr.hhplus.be.server.domain.couponuser.CouponUserCommand
import kr.hhplus.be.server.domain.couponevent.CouponEventBenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEventCommand
import kr.hhplus.be.server.domain.couponevent.CouponEventException

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
        val benefitMethod: CouponEventBenefitMethod,
        val benefitAmount: String,
        val totalIssueAmount: Long
    ) {
        /**
         * 도메인 Command로 변환
         */
        fun toCommand(): CouponEventCommand.Create {
            return CouponEventCommand.Create(
                benefitMethod = benefitMethod,
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
        fun toCommand(benefitMethod: CouponEventBenefitMethod, benefitAmount: String): CouponUserCommand.Create {
            // BenefitMethod를 CouponBenefitMethod로 변환
            val couponBenefitMethod = when (benefitMethod) {
                CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT -> CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT
                CouponEventBenefitMethod.DISCOUNT_PERCENTAGE -> CouponUserBenefitMethod.DISCOUNT_PERCENTAGE
            }
            
            return CouponUserCommand.Create(
                userId = userId,
                benefitMethod = couponBenefitMethod,
                benefitAmount = benefitAmount
            )
        }
    }
} 