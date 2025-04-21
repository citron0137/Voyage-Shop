package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.couponuser.CouponUser
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import java.time.LocalDateTime

/**
 * 쿠폰 유저 관련 결과를 담는 클래스
 */
sealed class CouponUserResult {
    /**
     * 단일 쿠폰 유저 결과 DTO
     */
    data class Single(
        val couponUserId: String,
        val userId: String,
        val benefitMethod: CouponUserBenefitMethod,
        val benefitAmount: String,
        val usedAt: LocalDateTime?,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : CouponUserResult() {
        companion object {
            /**
             * CouponUser 도메인 객체를 CouponUserResult.Single DTO로 변환합니다.
             */
            fun from(couponUser: CouponUser): Single {
                return Single(
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
        val couponUsers: kotlin.collections.List<Single>
    ) : CouponUserResult() {
        companion object {
            /**
             * CouponUser 도메인 객체 목록을 CouponUserResult.List DTO로 변환합니다.
             */
            fun from(couponUsers: kotlin.collections.List<CouponUser>): List {
                return List(
                    couponUsers = couponUsers.map { Single.from(it) }
                )
            }
        }
    }
} 