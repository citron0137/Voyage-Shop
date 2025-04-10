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
     * @param userId 조회할 사용자 ID
     * @return 사용자 포인트 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getUserPoint(userId: String): UserPointResult {
        // 사용자 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        // 사용자 포인트 조회
        val userPoint = userPointService.findByUserId(userId) 
            ?: throw UserPointException.NotFound("userId($userId)로 UserPoint를 찾을 수 없습니다.")
        
        return UserPointResult.from(userPoint)
    }
    
    /**
     * 사용자 포인트를 충전합니다.
     *
     * @param userId 사용자 ID
     * @param amount 충전할 포인트 금액
     * @return 충전 후 사용자 포인트 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     * @throws UserPointException.ChargeAmountShouldMoreThan0 충전 금액이 0 이하인 경우
     * @throws UserPointException.PointAmountOverflow 충전 후 포인트가 최대치를 초과하는 경우
     */
    @Transactional
    fun chargePoint(userId: String, amount: Long): UserPointResult {
        // 사용자 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        // 포인트 충전 명령 생성
        val command = UserPointCommand.Charge(userId = userId, amount = amount)
        
        // 포인트 충전
        val userPoint = userPointService.charge(command)
            ?: throw UserPointException.NotFound("userId($userId)로 UserPoint를 찾을 수 없습니다.")
        
        return UserPointResult.from(userPoint)
    }
} 