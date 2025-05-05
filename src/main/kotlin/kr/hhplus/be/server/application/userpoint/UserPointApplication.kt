package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.userpoint.UserPointException
import kr.hhplus.be.server.domain.userpoint.UserPointService
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.springframework.stereotype.Component

/**
 * 사용자 포인트 애플리케이션 서비스
 * 여러 도메인 서비스를 조합하여 포인트 조회, 충전, 사용 등의 비즈니스 유스케이스를 구현하고 트랜잭션을 관리합니다.
 */
@Component
class UserPointApplication(
    private val userPointService: UserPointService,
    private val transactionHelper: TransactionHelper,
) {
    /**
     * 사용자 ID로 포인트 정보를 조회합니다.
     *
     * @param criteria 사용자 포인트 조회 요청 기준
     * @return 사용자 포인트 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     */
    fun getUserPoint(criteria: UserPointCriteria.GetByUserId): UserPointResult.Single {
        return transactionHelper.executeInReadOnlyTransaction {
            // 사용자 포인트 조회
            val userPoint = userPointService.getByUserId(criteria.toQuery())
            UserPointResult.Single.from(userPoint)
        }
    }
    
    /**
     * 사용자 포인트를 충전합니다.
     *
     * @param criteria 사용자 포인트 충전 요청 기준
     * @return 충전 후 사용자 포인트 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     * @throws UserPointException.ChargeAmountShouldMoreThan0 충전 금액이 0 이하인 경우
     * @throws UserPointException.PointAmountOverflow 충전 후 포인트가 최대치를 초과하는 경우
     */
    @DistributedLock(
        domain = LockKeyConstants.USER_POINT_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_USER,
        resourceIdExpression = "criteria.userId"
    )
    fun chargePoint(criteria: UserPointCriteria.Charge): UserPointResult.Single {
        return transactionHelper.executeInTransaction {
            // 포인트 충전
            val userPoint = userPointService.charge(criteria.toCommand())
            UserPointResult.Single.from(userPoint)
        }
    }
    
    /**
     * 사용자 포인트를 사용합니다.
     * 비관적 락을 사용하여 동시성 문제를 방지합니다.
     *
     * @param criteria 사용자 포인트 사용 요청 기준
     * @return 사용 후 사용자 포인트 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     * @throws UserPointException.UseAmountShouldMoreThan0 사용 금액이 0 이하인 경우
     * @throws UserPointException.PointAmountUnderflow 사용 가능한 포인트가 부족한 경우
     */
    @DistributedLock(
        domain = LockKeyConstants.USER_POINT_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_USER,
        resourceIdExpression = "criteria.userId"
    )
    fun usePoint(criteria: UserPointCriteria.Use): UserPointResult.Single {
        return transactionHelper.executeInTransaction {
            // 포인트 사용
            val userPoint = userPointService.use(criteria.toCommand())
            UserPointResult.Single.from(userPoint)
        }
    }
} 