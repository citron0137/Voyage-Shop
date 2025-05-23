package kr.hhplus.be.server.domain.couponuser

import java.time.LocalDateTime
import java.util.UUID

/**
 * 쿠폰 유저 관련 명령 객체
 */
sealed class CouponUserCommand {
    /**
     * 유저 ID로 쿠폰 조회 명령
     */
    data class GetByUserId(
        val userId: String
    ) : CouponUserCommand() {
        init {
            if (userId.isBlank()) {
                throw CouponUserException.UserIdShouldNotBlank("유저 ID는 비어있을 수 없습니다.")
            }
        }
    }
    
    /**
     * 모든 쿠폰 조회 명령
     */
    class GetAll : CouponUserCommand()
    
    /**
     * 쿠폰 ID로 조회 명령
     */
    data class GetById(
        val couponUserId: String
    ) : CouponUserCommand() {
        init {
            if (couponUserId.isBlank()) {
                throw CouponUserException.CouponUserIdShouldNotBlank("쿠폰 ID는 비어있을 수 없습니다.")
            }
        }
    }

    /**
     * 쿠폰 발급 명령
     */
    data class Create(
        val userId: String,
        val benefitMethod: CouponUserBenefitMethod,
        val benefitAmount: String
    ) : CouponUserCommand() {
        init {
            if (userId.isBlank()) {
                throw CouponUserException.UserIdShouldNotBlank(userId)
            }
            
            if (benefitAmount.isBlank()) {
                throw CouponUserException.BenefitAmountShouldNotBlank(benefitAmount)
            }
            if (benefitAmount.toLongOrNull() == null) {
                throw CouponUserException.BenefitAmountShouldBeNumeric(benefitAmount)
            }
            if (benefitAmount.toLong() <= 0) {
                throw CouponUserException.BenefitAmountShouldMoreThan0(benefitAmount)
            }
            
            // 퍼센트 할인율이 100%를 초과하는지 확인
            if (benefitMethod == CouponUserBenefitMethod.DISCOUNT_PERCENTAGE && benefitAmount.toLong() > 100) {
                throw CouponUserException.DiscountPercentageExceeds100("Discount percentage ${benefitAmount} exceeds 100%")
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

    /**
     * 쿠폰 사용 명령
     */
    data class Use(
        val couponUserId: String
    ) : CouponUserCommand() {
        init {
            if (couponUserId.isBlank()) {
                throw CouponUserException.CouponUserIdShouldNotBlank(couponUserId)
            }
        }
    }

    /**
     * 쿠폰 할인 계산 명령
     */
    data class CalculateDiscount(
        val couponUserId: String,
        val originalAmount: Long
    ) : CouponUserCommand() {
        init {
            if (couponUserId.isBlank()) {
                throw CouponUserException.CouponUserIdShouldNotBlank("쿠폰 ID는 비어있을 수 없습니다.")
            }
            if (originalAmount <= 0) {
                throw CouponUserException.InvalidOriginalAmount("원래 금액은 0보다 커야 합니다: $originalAmount")
            }
        }
    }
} 