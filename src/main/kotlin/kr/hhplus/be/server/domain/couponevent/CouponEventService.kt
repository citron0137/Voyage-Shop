package kr.hhplus.be.server.domain.couponevent

import kr.hhplus.be.server.application.couponevent.CouponEventCriteria
import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUser
import kr.hhplus.be.server.domain.coupon.CouponUserCommand
import kr.hhplus.be.server.domain.coupon.CouponUserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponEventService(
    private val repository: CouponEventRepository
) {
    fun createCouponEvent(command: CreateCouponEventCommand): CouponEvent {
        return repository.create(command.toEntity())
    }

    /**
     * 쿠폰 이벤트를 검증하고 생성합니다.
     *
     * @param criteria 쿠폰 이벤트 생성 요청 정보
     * @return 생성된 쿠폰 이벤트
     * @throws CEInvalidBenefitMethodException 유효하지 않은 혜택 방식인 경우
     */
    fun createCouponEventWithValidation(criteria: CouponEventCriteria.Create): CouponEvent {
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
        
        return createCouponEvent(command)
    }

    fun getCouponEvent(id: String): CouponEvent {
        return repository.findById(id)
            ?: throw CENotFoundException(id)
    }

    fun getAllCouponEvents(): List<CouponEvent> {
        return repository.findAll()
    }

    /**
     * 쿠폰 이벤트의 재고를 감소시킵니다.
     *
     * @param id 쿠폰 이벤트 ID
     * @return 업데이트된 쿠폰 이벤트
     * @throws CENotFoundException 쿠폰 이벤트를 찾을 수 없는 경우
     * @throws CEOutOfStockException 재고가 없는 경우
     */
    @Transactional
    fun decreaseStock(id: String): CouponEvent {
        val couponEvent = getCouponEvent(id)
        couponEvent.validateCanIssue()
        return repository.decreaseStock(id)
            ?: throw CEOutOfStockException("Coupon event $id is out of stock")
    }

    /**
     * 쿠폰 이벤트로부터 쿠폰을 발급합니다.
     * 쿠폰 이벤트의 재고를 감소시키고 쿠폰 사용자를 생성합니다.
     *
     * @param couponEventId 쿠폰 이벤트 ID
     * @param criteria 쿠폰 발급 요청 정보
     * @param couponUserService 쿠폰 유저 서비스
     * @return 발급된 쿠폰 정보
     * @throws CENotFoundException 쿠폰 이벤트를 찾을 수 없는 경우
     * @throws CEOutOfStockException 재고가 없는 경우
     */
    @Transactional
    fun issueCouponFromEvent(
        couponEventId: String,
        criteria: CouponEventCriteria.IssueCoupon,
        couponUserService: CouponUserService
    ): CouponUser {
        // 1. 쿠폰 이벤트 조회 및 존재 여부 확인
        val couponEvent = getCouponEvent(couponEventId)

        // 2. 재고 확인 (이 시점에 재고 없으면 예외 발생)
        couponEvent.validateCanIssue()

        // 3. 재고 감소 시도 (실패 시 예외 발생)
        val updatedCouponEvent = decreaseStock(couponEventId)

        // 4. 쿠폰 유저 생성 (실제 CouponUserService 호출)
        val benefitMethod = convertToCouponBenefitMethod(couponEvent.benefitMethod)

        val createCommand = CouponUserCommand.Create(
            userId = criteria.userId,
            benefitMethod = benefitMethod,
            benefitAmount = couponEvent.benefitAmount
        )

        return couponUserService.create(createCommand)
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