package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.couponevent.*
import kr.hhplus.be.server.domain.couponuser.CouponUserService
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

/**
 * 쿠폰 이벤트 관련 유스케이스를 조합하는 퍼사드 클래스
 * 쿠폰 이벤트 생성, 조회, 쿠폰 발급 등의 기능을 제공합니다.
 */
@Component
class CouponEventFacade(
    private val couponEventService: CouponEventService,
    private val couponUserService: CouponUserService,
    private val lockManager: DistributedLockManager,
    private val transactionHelper: TransactionHelper
) {
    /**
     * 쿠폰 이벤트를 생성합니다.
     * 
     * @param criteria 쿠폰 이벤트 생성 요청 기준
     * @return 생성된 쿠폰 이벤트 정보
     */
    fun createCouponEvent(criteria: CouponEventCriteria.Create): CouponEventResult.Single {
        return transactionHelper.executeInTransaction {
            // Criteria에서 Command로 변환
            val command = criteria.toCommand()
            
            val couponEvent = couponEventService.createCouponEvent(command)
            CouponEventResult.Single.from(couponEvent)
        }
    }

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     * 
     * @param criteria 쿠폰 이벤트 조회 요청 기준 (선택 사항)
     * @return 쿠폰 이벤트 목록
     */
    fun getAllCouponEvents(criteria: CouponEventCriteria.GetAll = CouponEventCriteria.GetAll()): CouponEventResult.List {
        return transactionHelper.executeInReadOnlyTransaction {
            val couponEvents = couponEventService.getAllCouponEvents(CouponEventQuery.GetAll())
            CouponEventResult.List.from(couponEvents)
        }
    }

    /**
     * ID로 쿠폰 이벤트를 조회합니다.
     * 
     * @param criteria 쿠폰 이벤트 조회 요청 기준
     * @return 쿠폰 이벤트 정보
     * @throws CouponEventException.NotFound 쿠폰 이벤트를 찾을 수 없는 경우
     */
    fun getCouponEvent(criteria: CouponEventCriteria.GetById): CouponEventResult.Single {
        return transactionHelper.executeInReadOnlyTransaction {
            val couponEvent = couponEventService.getCouponEvent(CouponEventQuery.GetById(criteria.couponEventId))
            CouponEventResult.Single.from(couponEvent)
        }
    }

    /**
     * 사용자에게 쿠폰을 발급합니다.
     * 분산 락을 사용하여 동시성 문제를 방지합니다.
     * 
     * @param criteria 쿠폰 발급 요청 기준
     * @return 발급된 쿠폰 정보
     * @throws CouponEventException.NotFound 쿠폰 이벤트를 찾을 수 없는 경우
     * @throws CouponEventException.OutOfStock 쿠폰 재고가 없는 경우
     * @throws kr.hhplus.be.server.domain.coupon.CouponUserException.AlreadyIssuedException 이미 발급된 쿠폰인 경우
     */
    @DistributedLock(key = "coupon-event", parameterName = "criteria.couponEventId")
    fun issueCouponUser(criteria: CouponEventCriteria.IssueCoupon): CouponEventResult.IssueCoupon {
        return transactionHelper.executeInTransaction {
            // 재고 감소 - 비관적 락으로 동시성 제어
            val updatedCouponEvent = couponEventService.decreaseStock(CouponEventCommand.Issue(criteria.couponEventId))

            // 쿠폰 유저 생성
            val createCommand =
                criteria.toCommand(updatedCouponEvent.benefitMethod, updatedCouponEvent.benefitAmount)
            val couponUser = couponUserService.create(createCommand)

            CouponEventResult.IssueCoupon.from(couponUser)
        }
    }
} 
