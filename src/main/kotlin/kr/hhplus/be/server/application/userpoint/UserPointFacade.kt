package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.userpoint.UserPointCommand
import kr.hhplus.be.server.domain.userpoint.UserPointException
import kr.hhplus.be.server.domain.userpoint.UserPointService
import org.springframework.stereotype.Component
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
    fun getUserPoint(criteria: UserPointCriteria.GetByUserId): UserPointResult.Point {
        // 사용자 존재 여부 확인
        userService.findUserByIdOrThrow(criteria.userId)
        
        // 사용자 포인트 조회
        val userPoint = userPointService.findByUserId(criteria.userId) 
            ?: throw UserPointException.NotFound("userId(${criteria.userId})로 UserPoint를 찾을 수 없습니다.")
        
        return UserPointResult.Point.from(userPoint)
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
    @Transactional
    fun chargePoint(criteria: UserPointCriteria.Charge): UserPointResult.Point {
        // 사용자 존재 여부 확인
        userService.findUserByIdOrThrow(criteria.userId)
        
        // 포인트 충전 명령 생성
        val command = UserPointCommand.Charge(userId = criteria.userId, amount = criteria.amount)
        
        // 포인트 충전
        val userPoint = userPointService.charge(command)
            ?: throw UserPointException.NotFound("userId(${criteria.userId})로 UserPoint를 찾을 수 없습니다.")
        
        return UserPointResult.Point.from(userPoint)
    }
} 