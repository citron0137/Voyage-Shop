package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.domain.couponevent.*
import kr.hhplus.be.server.domain.couponuser.CouponUserService
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.springframework.stereotype.Component

/**
 * 쿠폰 이벤트 관련 애플리케이션 서비스 클래스
 * 여러 도메인 서비스를 조합하여 쿠폰 이벤트 생성, 조회, 쿠폰 발급 등의 비즈니스 유스케이스를 구현합니다.
 */
@Component
class CouponEventApplication(
    private val couponEventService: CouponEventService,
    private val couponUserService: CouponUserService,
    private val transactionHelper: TransactionHelper,
    private val lockManager: DistributedLockManager
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
     
    // @DistributedLock(
    //     domain = LockKeyConstants.COUPON_EVENT_PREFIX,
    //     resourceType = LockKeyConstants.RESOURCE_ID,
    //     resourceIdExpression = "criteria.couponEventId",
    //     timeout = LockKeyConstants.EXTENDED_TIMEOUT
    // )
    fun issueCouponUser(criteria: CouponEventCriteria.IssueCoupon): CouponEventResult.IssueCoupon {
        // 재고 감소 - 분산 락으로 동시성 제어
        val updatedCouponEvent = lockManager.executeWithDomainLock(
            domainPrefix = LockKeyConstants.COUPON_EVENT_PREFIX,
            resourceType = LockKeyConstants.RESOURCE_ID,
            resourceId = criteria.couponEventId
        )  {
            transactionHelper.executeInTransaction {              
                couponEventService.decreaseStock(CouponEventCommand.Issue(criteria.couponEventId))
            }
        }
        // 쿠폰 유저 생성
        val createCommand =
            criteria.toCommand(updatedCouponEvent.benefitMethod, updatedCouponEvent.benefitAmount)
        val couponUser = couponUserService.create(createCommand)
        return  CouponEventResult.IssueCoupon.from(couponUser)
    }

    fun updateRdb(){ couponEventService.updateRdb() }
}
