package kr.hhplus.be.server.domain.user

import java.time.LocalDateTime

/**
 * 사용자 정보 응답 관련 클래스
 */
sealed class UserInfo {
    /**
     * 단일 사용자 정보
     */
    data class UserDetail(
        val userId: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : UserInfo() {
        companion object {
            fun from(user: User): UserDetail {
                return UserDetail(
                    userId = user.userId,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
        }
    }
    
    /**
     * 사용자 목록 정보
     */
    data class UserList(val users: List<UserDetail>) : UserInfo() {
        companion object {
            fun from(users: List<User>): UserList {
                return UserList(users = users.map { UserDetail.from(it) })
            }
        }
    }
} 