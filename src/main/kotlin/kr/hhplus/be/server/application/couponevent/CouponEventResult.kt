package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.coupon.CouponUser
import kr.hhplus.be.server.domain.couponevent.BenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEvent
import java.time.LocalDateTime

/**
 * 쿠폰 이벤트 관련 결과를 담는 클래스
 */
sealed class CouponEventResult {
    /**
     * 단일 쿠폰 이벤트 조회 결과 DTO
     */
    data class Get(
        val id: String,
        val benefitMethod: BenefitMethod,
        val benefitAmount: String,
        val totalIssueAmount: Int,
        val leftIssueAmount: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : CouponEventResult() {
        companion object {
            /**
             * CouponEvent 도메인 객체를 CouponEventResult.Get DTO로 변환합니다.
             */
            fun from(couponEvent: CouponEvent): Get {
                return Get(
                    id = couponEvent.id,
                    benefitMethod = couponEvent.benefitMethod,
                    benefitAmount = couponEvent.benefitAmount,
                    totalIssueAmount = couponEvent.totalIssueAmount,
                    leftIssueAmount = couponEvent.leftIssueAmount,
                    createdAt = couponEvent.createdAt,
                    updatedAt = couponEvent.updatedAt
                )
            }
        }
    }

    /**
     * 쿠폰 이벤트 목록 조회 결과 DTO
     */
    data class List(
        val couponEvents: kotlin.collections.List<Get>
    ) : CouponEventResult() {
        companion object {
            /**
             * CouponEvent 도메인 객체 목록을 CouponEventResult.List DTO로 변환합니다.
             */
            fun from(couponEvents: kotlin.collections.List<CouponEvent>): List {
                return List(
                    couponEvents = couponEvents.map { Get.from(it) }
                )
            }
        }
    }

    /**
     * 쿠폰 발급 결과 DTO
     */
    data class IssueCoupon(
        val couponUserId: String
    ) : CouponEventResult() {
        companion object {
            /**
             * CouponUser 도메인 객체를 CouponEventResult.IssueCoupon DTO로 변환합니다.
             */
            fun from(couponUser: CouponUser): IssueCoupon {
                return IssueCoupon(
                    couponUserId = couponUser.couponUserId
                )
            }
        }
    }
} 