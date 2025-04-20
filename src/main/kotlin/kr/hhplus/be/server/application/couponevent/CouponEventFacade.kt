package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.coupon.CouponUserService
import kr.hhplus.be.server.domain.couponevent.CEInvalidBenefitMethodException
import kr.hhplus.be.server.domain.couponevent.CouponEventService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 이벤트 관련 유스케이스를 조합하는 퍼사드 클래스
 * 쿠폰 이벤트 생성, 조회, 쿠폰 발급 등의 기능을 제공합니다.
 */
@Component
class CouponEventFacade(
    private val couponEventService: CouponEventService,
    private val couponUserService: CouponUserService
) {
    
    /**
     * 쿠폰 이벤트를 생성합니다.
     * 
     * @param criteria 쿠폰 이벤트 생성 요청 기준
     * @return 생성된 쿠폰 이벤트 정보
     * @throws CEInvalidBenefitMethodException 유효하지 않은 혜택 유형인 경우
     */
    @Transactional
    fun createCouponEvent(criteria: CouponEventCriteria.Create): CouponEventResult.Single {
        // Criteria에서 Command로 변환
        val command = criteria.toCommand()
        
        val couponEvent = couponEventService.createCouponEvent(command)
        return CouponEventResult.Single.from(couponEvent)
    }

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     * 
     * @param criteria 쿠폰 이벤트 조회 요청 기준 (선택 사항)
     * @return 쿠폰 이벤트 목록
     */
    @Transactional(readOnly = true)
    fun getAllCouponEvents(criteria: CouponEventCriteria.GetAll = CouponEventCriteria.GetAll()): CouponEventResult.List {
        val couponEvents = couponEventService.getAllCouponEvents()
        return CouponEventResult.List.from(couponEvents)
    }

    /**
     * ID로 쿠폰 이벤트를 조회합니다.
     * 
     * @param criteria 쿠폰 이벤트 조회 요청 기준
     * @return 쿠폰 이벤트 정보
     * @throws kr.hhplus.be.server.domain.couponevent.CENotFoundException 쿠폰 이벤트를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getCouponEvent(criteria: CouponEventCriteria.GetById): CouponEventResult.Single {
        val couponEvent = couponEventService.getCouponEvent(criteria.couponEventId)
        return CouponEventResult.Single.from(couponEvent)
    }

    /**
     * 사용자에게 쿠폰을 발급합니다.
     * 
     * @param criteria 쿠폰 발급 요청 기준
     * @return 발급된 쿠폰 정보
     * @throws kr.hhplus.be.server.domain.couponevent.CENotFoundException 쿠폰 이벤트를 찾을 수 없는 경우
     * @throws kr.hhplus.be.server.domain.couponevent.CEOutOfStockException 쿠폰 재고가 없는 경우
     * @throws kr.hhplus.be.server.domain.coupon.CouponUserException.AlreadyIssuedException 이미 발급된 쿠폰인 경우
     */
    @Transactional
    fun issueCouponUser(criteria: CouponEventCriteria.IssueCoupon): CouponEventResult.IssueCoupon {
        // 1. 쿠폰 이벤트 조회 및 존재 여부 확인
        val couponEvent = couponEventService.getCouponEvent(criteria.couponEventId)
        
        // 2. 재고 확인 (이 시점에 재고 없으면 예외 발생)
        couponEvent.validateCanIssue()
        
        // 3. 재고 감소 시도 (실패 시 예외 발생)
        couponEventService.decreaseStock(criteria.couponEventId)
        
        // 4. 쿠폰 유저 생성 (실제 CouponUserService 호출)
        // Criteria에서 Command로 변환 (내부에서 BenefitMethod 변환도 수행)
        val createCommand = criteria.toCommand(couponEvent.benefitMethod, couponEvent.benefitAmount)
        
        val couponUser = couponUserService.create(createCommand)
        return CouponEventResult.IssueCoupon.from(couponUser)
    }
} 
