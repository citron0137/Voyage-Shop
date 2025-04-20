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
    ) {
        init {
            require(couponEventId.isNotBlank()) { "쿠폰 이벤트 ID는 비어있을 수 없습니다." }
        }
    }
    
    /**
     * 쿠폰 이벤트 생성 요청
     */
    data class Create(
        val benefitMethod: String,
        val benefitAmount: String,
        val totalIssueAmount: Long
    ) {
        init {
            require(benefitMethod.isNotBlank()) { "혜택 방식은 비어있을 수 없습니다." }
            require(benefitAmount.isNotBlank()) { "혜택 금액은 비어있을 수 없습니다." }
            require(totalIssueAmount > 0) { "총 발급 수량은 0보다 커야 합니다." }
            
            try {
                if (benefitMethod == "DISCOUNT_PERCENTAGE") {
                    val percentage = benefitAmount.toInt()
                    require(percentage in 1..100) { "할인율은 1%에서 100% 사이여야 합니다." }
                } else if (benefitMethod == "DISCOUNT_FIXED_AMOUNT") {
                    val amount = benefitAmount.toLong()
                    require(amount > 0) { "고정 할인 금액은 0보다 커야 합니다." }
                }
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("혜택 금액은 숫자여야 합니다.")
            }
        }
        
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
        init {
            require(couponEventId.isNotBlank()) { "쿠폰 이벤트 ID는 비어있을 수 없습니다." }
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
        }
        
        /**
         * 이 클래스에서는 Command를 생성하지 않고, 필요한 데이터만 제공합니다.
         * 실제 CouponUserCommand 생성은 Facade에서 수행합니다.
         */
    }
} 