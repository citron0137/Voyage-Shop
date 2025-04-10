package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUser
import java.time.LocalDateTime

/**
 * 쿠폰 유저 결과 DTO
 */
data class CouponUserResult(
    val couponUserId: String,
    val userId: String,
    val benefitMethod: CouponBenefitMethod,
    val benefitAmount: String,
    val usedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * CouponUser 도메인 객체를 CouponUserResult DTO로 변환합니다.
         */
        fun from(couponUser: CouponUser): CouponUserResult {
            return CouponUserResult(
                couponUserId = couponUser.couponUserId,
                userId = couponUser.userId,
                benefitMethod = couponUser.benefitMethod,
                benefitAmount = couponUser.benefitAmount,
                usedAt = couponUser.usedAt,
                createdAt = couponUser.createdAt,
                updatedAt = couponUser.updatedAt
            )
        }
    }
}

/**
 * 쿠폰 유저 목록 결과 DTO
 */
data class CouponUserListResult(
    val couponUsers: List<CouponUserResult>
) {
    companion object {
        /**
         * CouponUser 도메인 객체 목록을 CouponUserListResult DTO로 변환합니다.
         */
        fun from(couponUsers: List<CouponUser>): CouponUserListResult {
            return CouponUserListResult(
                couponUsers = couponUsers.map { CouponUserResult.from(it) }
            )
        }
    }
} 