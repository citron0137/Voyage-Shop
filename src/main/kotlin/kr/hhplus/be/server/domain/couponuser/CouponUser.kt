package kr.hhplus.be.server.domain.couponuser

import java.time.LocalDateTime

class CouponUser(
    val couponUserId: String,
    val userId: String,
    val benefitMethod: CouponUserBenefitMethod,
    val benefitAmount: String,
    val usedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun use(): CouponUser {
        if (usedAt != null) {
            throw CouponUserException.AlreadyUsed("Coupon already used")
        }
        return copy(usedAt = LocalDateTime.now())
    }

    fun calculateDiscountAmount(originalAmount: Long): Long {
        val calculator = CouponUserDiscountCalculatorFactory.getCalculator(benefitMethod)
        return calculator.calculate(originalAmount, benefitAmount)
    }

    private fun copy(
        couponUserId: String = this.couponUserId,
        userId: String = this.userId,
        benefitMethod: CouponUserBenefitMethod = this.benefitMethod,
        benefitAmount: String = this.benefitAmount,
        usedAt: LocalDateTime? = this.usedAt,
        createdAt: LocalDateTime = this.createdAt,
        updatedAt: LocalDateTime = this.updatedAt
    ): CouponUser {
        return CouponUser(
            couponUserId = couponUserId,
            userId = userId,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            usedAt = usedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
} 