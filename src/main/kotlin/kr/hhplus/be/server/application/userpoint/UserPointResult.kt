package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.userpoint.UserPoint
import java.time.LocalDateTime

/**
 * 사용자 포인트 결과 DTO
 */
data class UserPointResult(
    val userPointId: String,
    val userId: String,
    val amount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * UserPoint 도메인 객체를 UserPointResult DTO로 변환합니다.
         */
        fun from(userPoint: UserPoint): UserPointResult {
            return UserPointResult(
                userPointId = userPoint.userPointId,
                userId = userPoint.userId,
                amount = userPoint.amount,
                createdAt = userPoint.createdAt,
                updatedAt = userPoint.updatedAt
            )
        }
    }
}

/**
 * 사용자 포인트 목록 결과 DTO
 */
data class UserPointListResult(
    val userPoints: List<UserPointResult>
) {
    companion object {
        /**
         * UserPoint 도메인 객체 목록을 UserPointListResult DTO로 변환합니다.
         */
        fun from(userPoints: List<UserPoint>): UserPointListResult {
            return UserPointListResult(
                userPoints = userPoints.map { UserPointResult.from(it) }
            )
        }
    }
} 