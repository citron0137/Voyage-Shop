package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod

/**
 * 쿠폰 유저 관련 요청 기준을 담는 클래스
 */
sealed class CouponUserCriteria {
    /**
     * 사용자 ID로 쿠폰 목록 조회 요청
     */
    data class GetByUserId(
        val userId: String
    ) : CouponUserCriteria() {
        init {
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
        }
    }

    /**
     * 모든 쿠폰 조회 요청
     */
    class GetAll : CouponUserCriteria()
    
    /**
     * 쿠폰 ID로 쿠폰 조회 요청
     */
    data class GetById(
        val couponUserId: String
    ) : CouponUserCriteria() {
        init {
            require(couponUserId.isNotBlank()) { "쿠폰 ID는 비어있을 수 없습니다." }
        }
    }

    /**
     * 쿠폰 발급 요청
     */
    data class Create(
        val userId: String,
        val benefitMethod: CouponBenefitMethod,
        val benefitAmount: String
    ) : CouponUserCriteria() {
        init {
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
            require(benefitAmount.isNotBlank()) { "혜택 금액은 비어있을 수 없습니다." }
            try {
                val amount = benefitAmount.toLong()
                require(amount > 0) { "혜택 금액은 0보다 커야 합니다." }
                if (benefitMethod == CouponBenefitMethod.DISCOUNT_PERCENTAGE) {
                    require(amount <= 100) { "할인율은 100%를 초과할 수 없습니다." }
                }
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("혜택 금액은 숫자여야 합니다.")
            }
        }
    }

    /**
     * 쿠폰 사용 요청
     */
    data class Use(
        val couponUserId: String
    ) : CouponUserCriteria() {
        init {
            require(couponUserId.isNotBlank()) { "쿠폰 ID는 비어있을 수 없습니다." }
        }
    }

    /**
     * 쿠폰 할인 금액 계산 요청
     */
    data class CalculateDiscount(
        val couponUserId: String,
        val originalAmount: Long
    ) : CouponUserCriteria() {
        init {
            require(couponUserId.isNotBlank()) { "쿠폰 ID는 비어있을 수 없습니다." }
            require(originalAmount > 0) { "원래 금액은 0보다 커야 합니다." }
        }
    }
} 