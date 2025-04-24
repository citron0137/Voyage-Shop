package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.couponuser.CouponUserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 유저 관련 유스케이스를 조합하는 퍼사드 클래스
 * 쿠폰 유저 조회, 발급, 사용 및 할인 계산 기능을 제공합니다.
 */
@Component
class CouponUserFacade(
    private val couponUserService: CouponUserService,
) {
    /**
     * 유저 ID로 해당 유저의 모든 쿠폰을 조회합니다.
     *
     * @param criteria 사용자 ID로 쿠폰 조회 요청 기준
     * @return 유저의 쿠폰 목록 정보
     */
    @Transactional(readOnly = true)
    fun getAllCouponsByUserId(criteria: CouponUserCriteria.GetByUserId): CouponUserResult.List {
        return CouponUserResult.List.from(couponUserService.getAllCouponsByUserId(criteria.toCommand()))
    }
    
    /**
     * 쿠폰 ID로 쿠폰 정보를 조회합니다.
     *
     * @param criteria 쿠폰 ID로 조회 요청 기준
     * @return 쿠폰 정보
     * @throws kr.hhplus.be.server.domain.coupon.CouponException.CouponNotFound 쿠폰을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getCouponUser(criteria: CouponUserCriteria.GetById): CouponUserResult.Single {
        return CouponUserResult.Single.from(couponUserService.getCouponUser(criteria.toCommand()))
    }
    
    /**
     * 사용자에게 쿠폰을 발급합니다.
     *
     * @param criteria 쿠폰 발급 요청 기준
     * @return 발급된 쿠폰 정보
     * @throws kr.hhplus.be.server.domain.coupon.CouponException.InvalidBenefitAmount 유효하지 않은 혜택 금액인 경우
     */
    @Transactional
    fun issueCoupon(criteria: CouponUserCriteria.Create): CouponUserResult.Single {
        return CouponUserResult.Single.from(couponUserService.create(criteria.toCommand()))
    }
    
    /**
     * 쿠폰을 사용합니다.
     *
     * @param criteria 쿠폰 사용 요청 기준
     * @return 사용된 쿠폰 정보
     * @throws kr.hhplus.be.server.domain.coupon.CouponException.CouponNotFound 쿠폰을 찾을 수 없는 경우
     * @throws kr.hhplus.be.server.domain.coupon.CouponException.CouponAlreadyUsed 쿠폰이 이미 사용된 경우
     */
    @Transactional
    fun useCoupon(criteria: CouponUserCriteria.Use): CouponUserResult.Single {
        return CouponUserResult.Single.from(couponUserService.useCoupon(criteria.toCommand()))
    }
    
    /**
     * 쿠폰으로 할인 금액을 계산합니다.
     *
     * @param criteria 쿠폰 할인 계산 요청 기준
     * @return 할인 금액
     * @throws kr.hhplus.be.server.domain.coupon.CouponException.CouponNotFound 쿠폰을 찾을 수 없는 경우
     * @throws kr.hhplus.be.server.domain.coupon.CouponException.InvalidOriginalAmount 유효하지 않은 원래 금액인 경우
     */
    @Transactional(readOnly = true)
    fun calculateDiscountAmount(criteria: CouponUserCriteria.CalculateDiscount): Long {
        return couponUserService.calculateDiscountAmount(criteria.toCommand())
    }

    /**
     * 모든 쿠폰을 조회합니다.
     *
     * @param criteria 모든 쿠폰 조회 요청 기준
     * @return 모든 쿠폰 목록 정보
     */
    @Transactional(readOnly = true)
    fun getAllCoupons(criteria: CouponUserCriteria.GetAll = CouponUserCriteria.GetAll()): CouponUserResult.List {
        return CouponUserResult.List.from(couponUserService.getAllCouponUsers(criteria.toCommand()))
    }
}