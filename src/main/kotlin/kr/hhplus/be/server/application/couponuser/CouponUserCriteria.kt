package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUserCommand

/**
 * 쿠폰 유저 관련 요청 기준을 담는 클래스
 */
sealed class CouponUserCriteria {
    /**
     * 유저 ID로 쿠폰 조회 요청 기준
     */
    data class GetByUserId(
        val userId: String
    ) : CouponUserCriteria() {
        /**
         * 도메인 Command로 변환
         *
         * @return CouponUserCommand.GetByUserId 객체
         */
        fun toCommand(): CouponUserCommand.GetByUserId {
            return CouponUserCommand.GetByUserId(userId)
        }
    }

    /**
     * 모든 쿠폰 조회 요청 기준
     */
    class GetAll : CouponUserCriteria() {
        /**
         * 도메인 Command로 변환
         *
         * @return CouponUserCommand.GetAll 객체
         */
        fun toCommand(): CouponUserCommand.GetAll {
            return CouponUserCommand.GetAll()
        }
    }

    /**
     * 쿠폰 ID로 조회 요청 기준
     */
    data class GetById(
        val couponUserId: String
    ) : CouponUserCriteria() {
        /**
         * 도메인 Command로 변환
         *
         * @return CouponUserCommand.GetById 객체
         */
        fun toCommand(): CouponUserCommand.GetById {
            return CouponUserCommand.GetById(couponUserId)
        }
    }

    /**
     * 쿠폰 발급 요청 기준
     */
    data class Create(
        val userId: String,
        val benefitMethod: String,  // String으로 받아 도메인 객체로 변환
        val benefitAmount: String
    ) : CouponUserCriteria() {
        /**
         * 도메인 Command로 변환
         *
         * @return CouponUserCommand.Create 객체
         */
        fun toCommand(): CouponUserCommand.Create {
            return CouponUserCommand.Create(
                userId = userId,
                benefitMethod = CouponBenefitMethod.valueOf(benefitMethod),
                benefitAmount = benefitAmount
            )
        }
    }

    /**
     * 쿠폰 사용 요청 기준
     */
    data class Use(
        val couponUserId: String
    ) : CouponUserCriteria() {
        /**
         * 도메인 Command로 변환
         *
         * @return CouponUserCommand.Use 객체
         */
        fun toCommand(): CouponUserCommand.Use {
            return CouponUserCommand.Use(couponUserId)
        }
    }

    /**
     * 쿠폰 할인 계산 요청 기준
     */
    data class CalculateDiscount(
        val couponUserId: String,
        val originalAmount: Long
    ) : CouponUserCriteria() {
        /**
         * 도메인 Command로 변환
         *
         * @return CouponUserCommand.CalculateDiscount 객체
         */
        fun toCommand(): CouponUserCommand.CalculateDiscount {
            return CouponUserCommand.CalculateDiscount(
                couponUserId = couponUserId,
                originalAmount = originalAmount
            )
        }
    }
} 