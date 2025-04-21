package kr.hhplus.be.server.controller.couponevent

import kr.hhplus.be.server.application.couponevent.CouponEventCriteria
import kr.hhplus.be.server.domain.couponevent.CouponEventBenefitMethod

/**
 * 쿠폰 이벤트 요청
 */
sealed class CouponEventRequest {
    /**
     * 쿠폰 이벤트 생성 요청
     */
    data class Create(
        val benefitMethod: String,
        val benefitAmount: String,
        val totalIssueAmount: Int
    ) : CouponEventRequest() {
        /**
         * Request를 CouponEventCriteria.Create로 변환합니다.
         * 
         * @return 변환된 CouponEventCriteria.Create 객체
         * @throws IllegalArgumentException 유효하지 않은 benefitMethod인 경우
         */
        fun toCriteria(): CouponEventCriteria.Create {
            // 문자열을 BenefitMethod 열거형으로 변환
            val benefitMethodEnum = when(benefitMethod) {
                "DISCOUNT_FIXED_AMOUNT" -> CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT
                "DISCOUNT_PERCENTAGE" -> CouponEventBenefitMethod.DISCOUNT_PERCENTAGE
                else -> throw IllegalArgumentException("유효하지 않은 할인 방식입니다: $benefitMethod")
            }
            
            return CouponEventCriteria.Create(
                benefitMethod = benefitMethodEnum,
                benefitAmount = benefitAmount,
                totalIssueAmount = totalIssueAmount.toLong()
            )
        }
    }
    
    /**
     * 쿠폰 발급 요청
     */
    data class IssueCoupon(
        val userId: String
    ) : CouponEventRequest() {
        /**
         * Request를 CouponEventCriteria.IssueCoupon로 변환합니다.
         * 
         * @param couponEventId 쿠폰 이벤트 ID
         * @return 변환된 CouponEventCriteria.IssueCoupon 객체
         */
        fun toCriteria(couponEventId: String): CouponEventCriteria.IssueCoupon {
            return CouponEventCriteria.IssueCoupon(
                couponEventId = couponEventId,
                userId = userId
            )
        }
    }
} 