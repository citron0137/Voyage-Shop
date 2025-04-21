package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.userpoint.UserPoint
import java.time.LocalDateTime

/**
 * 사용자 포인트 관련 응답 결과를 담는 클래스
 */
class UserPointResult {
    /**
     * 단일 사용자 포인트 정보 응답
     */
    data class Single(
        val userPointId: String,
        val userId: String,
        val amount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            /**
             * UserPoint 도메인 객체를 Single DTO로 변환합니다.
             */
            fun from(userPoint: UserPoint): Single {
                return Single(
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
     * 사용자 포인트 목록 응답
     */
    data class List(
        val userPoints: kotlin.collections.List<Single>
    ) {
        companion object {
            /**
             * UserPoint 도메인 객체 목록을 List DTO로 변환합니다.
             */
            fun from(userPoints: kotlin.collections.List<UserPoint>): List {
                return List(
                    userPoints = userPoints.map { Single.from(it) }
                )
            }
        }
    }
} 