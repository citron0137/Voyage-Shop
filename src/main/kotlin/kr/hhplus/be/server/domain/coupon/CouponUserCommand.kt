package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime
import java.util.UUID

sealed class CouponUserCommand {
    data class Create(
        val userId: String,
        val benefitMethod: CouponBenefitMethod,
        val benefitAmount: String
    ) : CouponUserCommand() {
        init {
            if (userId.isBlank()) {
                throw CouponException.UserIdShouldNotBlank(userId)
            }
            
            if (benefitAmount.isBlank()) {
                throw CouponException.BenefitAmountShouldNotBlank(benefitAmount)
            }
            if (benefitAmount.toLongOrNull() == null) {
                throw CouponException.BenefitAmountShouldBeNumeric(benefitAmount)
            }
            if (benefitAmount.toLong() <= 0) {
                throw CouponException.BenefitAmountShouldMoreThan0(benefitAmount)
            }
            
            // 퍼센트 할인율이 100%를 초과하는지 확인
            if (benefitMethod == CouponBenefitMethod.DISCOUNT_PERCENTAGE && benefitAmount.toLong() > 100) {
                throw CouponException.DiscountPercentageExceeds100("Discount percentage ${benefitAmount} exceeds 100%")
            }
        }

        fun toEntity(): CouponUser {
            return CouponUser(
                couponUserId = UUID.randomUUID().toString(),
                userId = userId,
                benefitMethod = benefitMethod,
                benefitAmount = benefitAmount,
                usedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }

    data class Use(
        val couponUserId: String
    ) : CouponUserCommand() {
        init {
            if (couponUserId.isBlank()) {
                throw CouponException.CouponUserIdShouldNotBlank(couponUserId)
            }
        }
    }
} 