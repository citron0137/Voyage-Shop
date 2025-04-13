package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.application.couponevent.dto.CouponEventDTO
import kr.hhplus.be.server.application.couponevent.dto.CreateCouponEventCriteria
import kr.hhplus.be.server.application.couponevent.dto.IssueCouponCriteria
import kr.hhplus.be.server.application.couponevent.dto.IssuedCouponDTO
import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUserCommand
import kr.hhplus.be.server.domain.coupon.CouponUserService
import kr.hhplus.be.server.domain.couponevent.BenefitMethod
import kr.hhplus.be.server.domain.couponevent.CEInvalidBenefitMethodException
import kr.hhplus.be.server.domain.couponevent.CreateCouponEventCommand
import kr.hhplus.be.server.domain.couponevent.CouponEventService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponEventFacade(
    private val couponEventService: CouponEventService,
    private val couponUserService: CouponUserService
) {
    
    fun createCouponEvent(criteria: CreateCouponEventCriteria): CouponEventDTO {
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
        
        return CouponEventDTO(
            id = couponEvent.id,
            benefitMethod = couponEvent.benefitMethod.name,
            benefitAmount = couponEvent.benefitAmount,
            totalIssueAmount = couponEvent.totalIssueAmount,
            leftIssueAmount = couponEvent.leftIssueAmount
        )
    }

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
     * 쿠폰 이벤트의 재고를 감소시키고 쿠폰 사용자를 생성합니다.
     * 재고가 없는 경우 예외가 발생합니다.
     * 
     * @param couponEventId 쿠폰 이벤트 ID
     * @param criteria 쿠폰 발급 요청 정보
     * @return 발급된 쿠폰 정보
     */
    @Transactional
    fun issueCouponUser(couponEventId: String, criteria: IssueCouponCriteria): IssuedCouponDTO {
        // 1. 쿠폰 이벤트 조회 및 존재 여부 확인
        val couponEvent = couponEventService.getCouponEvent(couponEventId)
        
        // 2. 재고 확인 (이 시점에 재고 없으면 예외 발생)
        couponEvent.validateCanIssue()
        
        // 3. 재고 감소 시도 (실패 시 예외 발생)
        val updatedCouponEvent = couponEventService.decreaseStock(couponEventId)
        
        // 4. 쿠폰 유저 생성 (실제 CouponUserService 호출)
        val benefitMethod = convertToCouponBenefitMethod(couponEvent.benefitMethod)
        
        val createCommand = CouponUserCommand.Create(
            userId = criteria.userId,
            benefitMethod = benefitMethod,
            benefitAmount = couponEvent.benefitAmount
        )
        
        val couponUser = couponUserService.create(createCommand)
        
        return IssuedCouponDTO(
            couponUserId = couponUser.couponUserId
        )
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