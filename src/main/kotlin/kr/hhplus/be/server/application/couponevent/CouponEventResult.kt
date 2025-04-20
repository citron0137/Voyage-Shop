package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.coupon.CouponUser
import kr.hhplus.be.server.domain.couponevent.BenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEvent
import java.time.LocalDateTime

/**
 * 쿠폰 이벤트 관련 결과를 담는 클래스
 */
class CouponEventResult {
    /**
     * 단일 쿠폰 이벤트 조회 결과 DTO
     */
    data class Single(
        val id: String,
        val benefitMethod: BenefitMethod,
        val benefitAmount: String,
        val totalIssueAmount: Long,
        val leftIssueAmount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            /**
             * CouponEvent 도메인 객체를 CouponEventResult.Single DTO로 변환합니다.
             */
            fun from(couponEvent: CouponEvent): Single {
                return Single(
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
        
        /**
         * 도메인 객체를 받아 생성하는 생성자
         */
        constructor(couponEvent: CouponEvent) : this(
            id = couponEvent.id,
            benefitMethod = couponEvent.benefitMethod,
            benefitAmount = couponEvent.benefitAmount,
            totalIssueAmount = couponEvent.totalIssueAmount,
            leftIssueAmount = couponEvent.leftIssueAmount,
            createdAt = couponEvent.createdAt,
            updatedAt = couponEvent.updatedAt
        )
    }

    /**
     * 쿠폰 이벤트 목록 조회 결과 DTO
     */
    data class List(
        val couponEvents: kotlin.collections.List<Single>
    ) {
        companion object {
            /**
             * CouponEvent 도메인 객체 목록을 CouponEventResult.List DTO로 변환합니다.
             */
            fun from(couponEvents: kotlin.collections.List<CouponEvent>): List {
                return List(
                    couponEvents = couponEvents.map { Single.from(it) }
                )
            }
        }
    }

    /**
     * 쿠폰 발급 결과 DTO
     */
    data class IssueCoupon(
        val couponUserId: String
    ) {
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
        
        /**
         * 도메인 객체를 받아 생성하는 생성자
         */
        constructor(couponUser: CouponUser) : this(
            couponUserId = couponUser.couponUserId
        )
    }
} 