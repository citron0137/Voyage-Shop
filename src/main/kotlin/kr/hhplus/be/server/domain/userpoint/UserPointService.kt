package kr.hhplus.be.server.domain.userpoint

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserPointService(
    private val userPointRepository: UserPointRepository,
) {

    final val MAX_USER_POINT = Long.MAX_VALUE

    // Create
    fun create(command: UserPointCommand.Create): UserPoint{
        val userPoint = UserPoint(
            userPointId = UUID.randomUUID().toString(),
            userId = command.userId,
            amount = 0
        )
        userPointRepository.create(userPoint)
        return userPoint
    }

    // Get
    fun findByUserId(userId: String): UserPoint? {
        return this.userPointRepository.findByUserId(userId)
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

    fun use(command: UserPointCommand.Use): UserPoint {
        val userPoint = userPointRepository.findByUserId(userId = command.userId)
            ?: throw UserPointException.NotFound("userId(${command.userId})로 UserPoint를 찾을 수 없습니다.")
        if( userPoint.amount - command.amount < 0 )
            throw UserPointException.PointAmountUnderflow("사용 가능 최대치를 초과했습니다.")
        userPoint.amount -= command.amount
        return userPointRepository.save(userPoint)
    }

}