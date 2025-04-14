package kr.hhplus.be.server.domain.userpoint

import java.time.LocalDateTime

/**
 * 사용자 포인트 정보 응답 관련 클래스
 */
sealed class UserPointInfo {
    /**
     * 사용자 포인트 상세 정보
     */
    data class UserPointDetail(
        val userPointId: String,
        val userId: String,
        val amount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : UserPointInfo() {
        companion object {
            fun from(userPoint: UserPoint): UserPointDetail {
                return UserPointDetail(
                    userPointId = userPoint.userPointId,
                    userId = userPoint.userId,
                    amount = userPoint.amount,
                    createdAt = userPoint.createdAt,
                    updatedAt = userPoint.updatedAt
                )
            }
        }
    }
} 