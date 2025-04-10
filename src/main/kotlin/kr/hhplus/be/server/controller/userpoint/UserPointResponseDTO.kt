package kr.hhplus.be.server.controller.userpoint.response

import kr.hhplus.be.server.application.userpoint.UserPointListResult
import kr.hhplus.be.server.application.userpoint.UserPointResult
import java.time.LocalDateTime

/**
 * 사용자 포인트 응답 DTO
 */
sealed class UserPointResponseDTO {
    /**
     * 단일 사용자 포인트 응답 DTO
     */
    data class Single(
        val id: String,
        val userId: String,
        val amount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : UserPointResponseDTO() {
        companion object {
            /**
             * UserPointResult를 UserPointResponseDTO.Single로 변환합니다.
             */
            fun from(result: UserPointResult): Single {
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
     * 사용자 포인트 목록 응답 DTO
     */
    data class List(
        val points: kotlin.collections.List<Single>
    ) : UserPointResponseDTO() {
        companion object {
            /**
             * UserPointListResult를 UserPointResponseDTO.List로 변환합니다.
             */
            fun from(result: UserPointListResult): List {
                return List(
                    points = result.userPoints.map { Single.from(it) }
                )
            }
        }
    }
}