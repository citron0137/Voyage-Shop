package kr.hhplus.be.server.controller.user

import kr.hhplus.be.server.application.user.UserResult
import java.time.LocalDateTime

/**
 * 사용자 관련 응답 객체들
 */
sealed class UserResponse {
    /**
     * 단일 사용자 응답
     */
    data class Single(
        val id: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : UserResponse() {
        companion object {
            fun from(result: UserResult.Single): Single {
                return Single(
                    id = result.userId,
                    createdAt = result.createdAt,
                    updatedAt = result.updatedAt
                )
            }
        }
    }

    /**
     * 사용자 목록 응답
     */
    data class List(
        val items: kotlin.collections.List<Single>
    ) : UserResponse() {
        companion object {
            fun from(result: UserResult.List): List {
                return List(
                    items = result.users.map { Single.from(it) }
                )
            }
        }
    }
} 