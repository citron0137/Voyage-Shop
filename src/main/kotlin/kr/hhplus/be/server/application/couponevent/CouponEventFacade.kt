package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUserCommand
import kr.hhplus.be.server.domain.coupon.CouponUserService
import kr.hhplus.be.server.domain.couponevent.BenefitMethod
import kr.hhplus.be.server.domain.couponevent.CEInvalidBenefitMethodException
import kr.hhplus.be.server.domain.couponevent.CouponEventService
import kr.hhplus.be.server.domain.couponevent.CreateCouponEventCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponEventFacade(
    private val couponEventService: CouponEventService,
    private val couponUserService: CouponUserService
) {
    
    /**
     * 쿠폰 이벤트를 생성합니다.
     * 
     * @param criteria 쿠폰 이벤트 생성 요청 기준
     * @return 생성된 쿠폰 이벤트 정보
     */
    @Transactional
    fun createCouponEvent(criteria: CouponEventCriteria.Create): CouponEventResult.Get {
        val benefitMethod = try {
            when (criteria.benefitMethod) {
                "DISCOUNT_FIXED_AMOUNT" -> BenefitMethod.DISCOUNT_FIXED_AMOUNT
                "DISCOUNT_PERCENTAGE" -> BenefitMethod.DISCOUNT_PERCENTAGE
                else -> throw CEInvalidBenefitMethodException(criteria.benefitMethod)
            }
        } catch (e: IllegalArgumentException) {
            throw CEInvalidBenefitMethodException(criteria.benefitMethod)
        }
        
        val command = CreateCouponEventCommand(
            benefitMethod = benefitMethod,
            benefitAmount = criteria.benefitAmount,
            totalIssueAmount = criteria.totalIssueAmount
        )
        
        val couponEvent = couponEventService.createCouponEvent(command)
        
        return CouponEventResult.Get.from(couponEvent)
    }

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     * 
     * @param criteria 쿠폰 이벤트 조회 요청 기준
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
     */
    @Transactional(readOnly = true)
    fun getCouponEvent(criteria: CouponEventCriteria.GetById): CouponEventResult.Get {
        val couponEvent = couponEventService.getCouponEvent(criteria.couponEventId)
        return CouponEventResult.Get.from(couponEvent)
    }

    /**
     * 쿠폰을 발급합니다.
     * 
     * @param criteria 쿠폰 발급 요청 기준
     * @return 발급된 쿠폰 정보
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
        val benefitMethod = convertToCouponBenefitMethod(couponEvent.benefitMethod)
        
        val createCommand = CouponUserCommand.Create(
            userId = criteria.userId,
            benefitMethod = benefitMethod,
            benefitAmount = couponEvent.benefitAmount
        )
        
        val couponUser = couponUserService.create(createCommand)
        
        return CouponEventResult.IssueCoupon.from(couponUser)
    }
    
    /**
     * CouponEvent의 BenefitMethod를 CouponUser의 CouponBenefitMethod로 변환합니다.
     */
    private fun convertToCouponBenefitMethod(benefitMethod: BenefitMethod): CouponBenefitMethod {
        return when (benefitMethod) {
            BenefitMethod.DISCOUNT_FIXED_AMOUNT -> CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT
            BenefitMethod.DISCOUNT_PERCENTAGE -> CouponBenefitMethod.DISCOUNT_PERCENTAGE
        }
    }
} 