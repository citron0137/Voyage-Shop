package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserQuery
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.userpoint.UserPointCommand
import kr.hhplus.be.server.domain.userpoint.UserPointException
import kr.hhplus.be.server.domain.userpoint.UserPointQuery
import kr.hhplus.be.server.domain.userpoint.UserPointService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 포인트 파사드
 * 사용자 포인트 관련 비즈니스 로직을 캡슐화하고 컨트롤러에서 사용할 수 있는 단순한 인터페이스를 제공합니다.
 */
@Component
class UserPointFacade(
    private val userPointService: UserPointService,
    private val userService: UserService
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
    @Transactional(readOnly = true)
    fun getUserPoint(criteria: UserPointCriteria.GetByUserId): UserPointResult.Single {
        // 사용자 포인트 조회
        val userPoint = userPointService.getByUserId(criteria.toQuery())
        return UserPointResult.Single.from(userPoint)
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
    fun chargePoint(criteria: UserPointCriteria.Charge): UserPointResult.Single {
        // 포인트 충전
        val userPoint = userPointService.charge(criteria.toCommand())
        return UserPointResult.Single.from(userPoint)
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
    fun usePoint(criteria: UserPointCriteria.Use): UserPointResult.Single {
        // 포인트 사용
        val userPoint = userPointService.use(criteria.toCommand())
        return UserPointResult.Single.from(userPoint)
    }
} 