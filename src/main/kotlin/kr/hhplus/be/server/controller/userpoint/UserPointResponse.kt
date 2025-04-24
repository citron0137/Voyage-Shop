package kr.hhplus.be.server.controller.userpoint

import kr.hhplus.be.server.application.userpoint.UserPointResult
import java.time.LocalDateTime

/**
 * 사용자 포인트 응답
 */
sealed class UserPointResponse {
    /**
     * 단일 사용자 포인트 응답
     */
    data class Single(
        val id: String,
        val userId: String,
        val amount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : UserPointResponse() {
        companion object {
            /**
             * UserPointResult.Point를 UserPointResponse.Single로 변환합니다.
             */
            fun from(result: UserPointResult.Single): Single {
                return Single(
                    id = result.userPointId,
                    userId = result.userId,
                    amount = result.amount,
                    createdAt = result.createdAt,
                    updatedAt = result.updatedAt
                )
            }
        }
    }

    /**
     * 사용자 포인트 목록 응답
     */
    data class List(
        val points: kotlin.collections.List<Single>
    ) : UserPointResponse() {
        companion object {
            /**
             * UserPointResult.List를 UserPointResponse.List로 변환합니다.
             */
            fun from(result: UserPointResult.List): List {
                return List(
                    points = result.userPoints.map { Single.from(it) }
                )
            }
        }
    }
}