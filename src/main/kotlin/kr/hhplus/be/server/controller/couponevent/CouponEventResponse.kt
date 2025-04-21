package kr.hhplus.be.server.controller.couponevent

import kr.hhplus.be.server.application.couponevent.CouponEventResult

/**
 * 쿠폰 이벤트 응답
 */
sealed class CouponEventResponse {
    /**
     * 쿠폰 이벤트 정보
     */
    data class Event(
        val id: String,
        val benefitMethod: String,
        val benefitAmount: String,
        val totalIssueAmount: Long,
        val leftIssueAmount: Long
    ) : CouponEventResponse() {
        companion object {
            /**
             * CouponEventResult.Single로부터 Event 응답 객체를 생성합니다.
             */
            fun from(result: CouponEventResult.Single): Event {
                return Event(
                    id = result.id,
                    benefitMethod = result.benefitMethod.name,
                    benefitAmount = result.benefitAmount,
                    totalIssueAmount = result.totalIssueAmount,
                    leftIssueAmount = result.leftIssueAmount
                )
            }
        }
    }
    
    /**
     * 쿠폰 발급 응답
     */
    data class IssueCoupon(
        val couponUserId: String
    ) : CouponEventResponse() {
        companion object {
            /**
             * CouponEventResult.IssueCoupon으로부터 IssueCoupon 응답 객체를 생성합니다.
             */
            fun from(result: CouponEventResult.IssueCoupon): IssueCoupon {
                return IssueCoupon(couponUserId = result.couponUserId)
            }
        }
    }
    
    /**
     * 쿠폰 이벤트 목록
     */
    data class List(
        val items: kotlin.collections.List<Event>
    ) : CouponEventResponse() {
        companion object {
            /**
             * Event 목록으로부터 List 응답 객체를 생성합니다.
             */
            fun from(events: kotlin.collections.List<Event>): List {
                return List(events)
            }
            
            /**
             * CouponEventResult.List로부터 List 응답 객체를 생성합니다.
             */
            fun from(result: CouponEventResult.List): List {
                return List(
                    items = result.couponEvents.map { Event.from(it) }
                )
            }
        }
    }
} 