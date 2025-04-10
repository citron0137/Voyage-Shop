package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.User
import java.time.LocalDateTime

/**
 * 사용자 조회/생성 결과를 담는 클래스
 */
data class UserResult(
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResult {
            return UserResult(
                userId = user.userId,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}

/**
 * 사용자 조회/생성 결과 목록을 담는 클래스
 */
data class UserListResult(
    val users: List<UserResult>
) {
    companion object {
        fun from(users: List<User>): UserListResult {
            return UserListResult(
                users = users.map { UserResult.from(it) }
            )
        }
    }
} 