package kr.hhplus.be.server.domain.userpoint

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserPointService(
    private val userPointRepository: UserPointRepository,
) {

    final val MAX_USER_POINT = Long.MAX_VALUE

    /**
     * 사용자 포인트를 생성합니다.
     * 
     * @param command 사용자 포인트 생성 명령
     * @return 생성된 사용자 포인트
     */
    fun create(command: UserPointCommand.Create): UserPoint {
        val userPoint = UserPoint(
            userPointId = UUID.randomUUID().toString(),
            userId = command.userId,
            amount = 0
        )
        return userPointRepository.create(userPoint)
    }

    /**
     * 사용자 ID로 포인트를 조회합니다.
     * 
     * @param query 사용자 ID로 포인트 조회 쿼리
     * @return 조회된 사용자 포인트(없으면 null)
     */
    fun getByUserId(query: UserPointQuery.GetByUserId): UserPoint {
        return this.userPointRepository.findByUserId(query.userId)
            ?: throw UserPointException.NotFound("userId(${query.userId})로 UserPoint를 찾을 수 없습니다.")
    }

    /**
     * 사용자 포인트를 충전합니다.
     * 락을 사용하여 동시성 문제를 방지합니다.
     * 
     * @param command 사용자 포인트 충전 명령
     * @return 충전 후 사용자 포인트
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     * @throws UserPointException.PointAmountOverflow 충전 가능 최대치를 초과한 경우
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun charge(command: UserPointCommand.Charge): UserPoint {
        val userPoint = userPointRepository.findByUserIdWithLock(userId = command.userId)
            ?: throw UserPointException.NotFound("userId(${command.userId})로 UserPoint를 찾을 수 없습니다.")
            
        if (userPoint.amount > MAX_USER_POINT - command.amount) {
            throw UserPointException.PointAmountOverflow("충전 가능 최대치를 초과했습니다.")
        }
        
        val chargedPoint = userPoint.charge(command.amount)
        return userPointRepository.save(chargedPoint)
    }

    /**
     * 사용자 포인트를 사용합니다.
     * 락을 사용하여 동시성 문제를 방지합니다.
     * 
     * @param command 사용자 포인트 사용 명령
     * @return 사용 후 사용자 포인트
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     * @throws UserPointException.PointAmountUnderflow 사용 가능한 포인트가 부족한 경우
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun use(command: UserPointCommand.Use): UserPoint {
        val userPoint = userPointRepository.findByUserIdWithLock(userId = command.userId)
            ?: throw UserPointException.NotFound("userId(${command.userId})로 UserPoint를 찾을 수 없습니다.")
            
        if (userPoint.amount - command.amount < 0) {
            throw UserPointException.PointAmountUnderflow("사용 가능 최대치를 초과했습니다.")
        }
        
        val usedPoint = userPoint.use(command.amount)
        return userPointRepository.save(usedPoint)
    }
}