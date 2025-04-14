package kr.hhplus.be.server.domain.userpoint

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserPointService(
    private val userPointRepository: UserPointRepository,
) {

    final val MAX_USER_POINT = Long.MAX_VALUE

    // Create
    fun create(command: UserPointCommand.Create): UserPoint {
        val userPoint = UserPoint(
            userPointId = UUID.randomUUID().toString(),
            userId = command.userId,
            amount = 0
        )
        userPointRepository.create(userPoint)
        return userPoint
    }

    /**
     * 사용자 포인트 생성 명령을 처리하고 정보를 반환합니다.
     * 
     * @param command 사용자 포인트 생성 명령
     * @return 생성된 사용자 포인트 정보
     */
    fun handle(command: UserPointCommand.Create): UserPointInfo.UserPointDetail {
        val userPoint = create(command)
        return UserPointInfo.UserPointDetail.from(userPoint)
    }

    // Get
    fun findByUserId(userId: String): UserPoint? {
        return this.userPointRepository.findByUserId(userId)
    }
    
    /**
     * 사용자 ID로 포인트 조회 쿼리를 처리합니다.
     * 
     * @param query 사용자 ID로 포인트 조회 쿼리
     * @return 조회된 사용자 포인트 정보(없으면 null)
     */
    fun handle(query: UserPointQuery.GetByUserId): UserPointInfo.UserPointDetail? {
        val userPoint = findByUserId(query.userId)
        return userPoint?.let { UserPointInfo.UserPointDetail.from(it) }
    }

    // Update
    fun charge(command: UserPointCommand.Charge): UserPoint {
        val userPoint = userPointRepository.findByUserId(userId = command.userId)
            ?: throw UserPointException.NotFound("userId(${command.userId})로 UserPoint를 찾을 수 없습니다.")
        if( userPoint.amount  > MAX_USER_POINT - command.amount )
            throw UserPointException.PointAmountOverflow("충전 가능 최대치를 초과했습니다.")
        userPoint.amount += command.amount
        return userPointRepository.save(userPoint)
    }

    /**
     * 사용자 포인트 충전 명령을 처리하고 정보를 반환합니다.
     * 
     * @param command 사용자 포인트 충전 명령
     * @return 충전 후 사용자 포인트 정보
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     * @throws UserPointException.PointAmountOverflow 충전 가능 최대치를 초과한 경우
     */
    fun handle(command: UserPointCommand.Charge): UserPointInfo.UserPointDetail {
        val userPoint = charge(command)
        return UserPointInfo.UserPointDetail.from(userPoint)
    }

    fun use(command: UserPointCommand.Use): UserPoint {
        val userPoint = userPointRepository.findByUserId(userId = command.userId)
            ?: throw UserPointException.NotFound("userId(${command.userId})로 UserPoint를 찾을 수 없습니다.")
        if( userPoint.amount - command.amount < 0 )
            throw UserPointException.PointAmountUnderflow("사용 가능 최대치를 초과했습니다.")
        userPoint.amount -= command.amount
        return userPointRepository.save(userPoint)
    }

    /**
     * 사용자 포인트 사용 명령을 처리하고 정보를 반환합니다.
     * 
     * @param command 사용자 포인트 사용 명령
     * @return 사용 후 사용자 포인트 정보
     * @throws UserPointException.NotFound 사용자 포인트를 찾을 수 없는 경우
     * @throws UserPointException.PointAmountUnderflow 사용 가능한 포인트가 부족한 경우
     */
    fun handle(command: UserPointCommand.Use): UserPointInfo.UserPointDetail {
        val userPoint = use(command)
        return UserPointInfo.UserPointDetail.from(userPoint)
    }
}