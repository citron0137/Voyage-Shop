package kr.hhplus.be.server.domain.couponevent

/**
 * 쿠폰 혜택 방식
 * - DISCOUNT_FIXED_AMOUNT: 고정 금액 할인
 * - DISCOUNT_PERCENTAGE: 퍼센트 할인
 */
enum class CouponEventBenefitMethod {
    DISCOUNT_FIXED_AMOUNT,
    DISCOUNT_PERCENTAGE
} 