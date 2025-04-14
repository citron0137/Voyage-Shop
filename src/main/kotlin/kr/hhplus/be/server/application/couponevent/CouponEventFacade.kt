package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.application.couponevent.dto.CouponEventDTO
import kr.hhplus.be.server.application.couponevent.dto.CreateCouponEventCriteria
import kr.hhplus.be.server.application.couponevent.dto.IssueCouponCriteria
import kr.hhplus.be.server.application.couponevent.dto.IssuedCouponDTO
import kr.hhplus.be.server.domain.coupon.CouponUserService
import kr.hhplus.be.server.domain.couponevent.CouponEventService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponEventFacade(
    private val couponEventService: CouponEventService,
    private val couponUserService: CouponUserService
) {
    
    /**
     * 쿠폰 이벤트를 생성합니다.
     */
    fun createCouponEvent(criteria: CreateCouponEventCriteria): CouponEventDTO {
        val couponEvent = couponEventService.createCouponEventWithValidation(criteria)
        
        return CouponEventDTO(
            id = couponEvent.id,
            benefitMethod = couponEvent.benefitMethod.name,
            benefitAmount = couponEvent.benefitAmount,
            totalIssueAmount = couponEvent.totalIssueAmount,
            leftIssueAmount = couponEvent.leftIssueAmount
        )
    }

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     */
    fun getAllCouponEvents(): List<CouponEventDTO> {
        return couponEventService.getAllCouponEvents().map {
            CouponEventDTO(
                id = it.id,
                benefitMethod = it.benefitMethod.name,
                benefitAmount = it.benefitAmount,
                totalIssueAmount = it.totalIssueAmount,
                leftIssueAmount = it.leftIssueAmount
            )
        }
    }

    /**
     * 쿠폰을 발급합니다.
     * 
     * @param couponEventId 쿠폰 이벤트 ID
     * @param criteria 쿠폰 발급 요청 정보
     * @return 발급된 쿠폰 정보
     */
    @Transactional
    fun issueCouponUser(couponEventId: String, criteria: IssueCouponCriteria): IssuedCouponDTO {
        val couponUser = couponEventService.issueCouponFromEvent(couponEventId, criteria, couponUserService)
        return IssuedCouponDTO(couponUserId = couponUser.couponUserId)
    }
} 