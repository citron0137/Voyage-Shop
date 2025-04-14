package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUser
import java.time.LocalDateTime

/**
 * 쿠폰 유저 관련 결과를 담는 클래스
 */
sealed class CouponUserResult {
    /**
     * 단일 쿠폰 유저 결과 DTO
     */
    data class User(
        val couponUserId: String,
        val userId: String,
        val benefitMethod: CouponBenefitMethod,
        val benefitAmount: String,
        val usedAt: LocalDateTime?,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : CouponUserResult() {
        companion object {
            /**
             * CouponUser 도메인 객체를 CouponUserResult.User DTO로 변환합니다.
             */
            fun from(couponUser: CouponUser): User {
                return User(
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
    data class List(
        val couponUsers: kotlin.collections.List<User>
    ) : CouponUserResult() {
        companion object {
            /**
             * CouponUser 도메인 객체 목록을 CouponUserResult.List DTO로 변환합니다.
             */
            fun from(couponUsers: kotlin.collections.List<CouponUser>): List {
                return List(
                    couponUsers = couponUsers.map { User.from(it) }
                )
            }
        }
    }
} 