package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.User
import java.time.LocalDateTime

/**
 * 사용자 관련 응답 결과를 담는 클래스
 */
class UserResult {
    /**
     * 단일 사용자 정보 응답
     */
    data class Single(
        val userId: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            fun from(user: User): Single {
                return Single(
                    userId = user.userId,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
        }
    }

    /**
     * 사용자 목록 응답
     */
    data class List(
        val users: kotlin.collections.List<Single>
    ) {
        companion object {
            fun from(users: kotlin.collections.List<User>): List {
                return List(
                    users = users.map { Single.from(it) }
                )
            }
        }
    }
} 