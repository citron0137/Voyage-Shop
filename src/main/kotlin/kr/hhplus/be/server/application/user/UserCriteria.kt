package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.UserCommand
import kr.hhplus.be.server.domain.user.UserQuery

/**
 * 사용자 관련 요청 기준을 담는 클래스
 */
class UserCriteria {
    /**
     * 사용자 생성 요청
     */
    class Create {
        fun toCommand(): UserCommand.Create {
            return UserCommand.Create
        }
    }

    /**
     * 사용자 조회 요청
     */
    data class GetById(
        val userId: String
    ) {
        fun toQuery(): UserQuery.GetById {
            return UserQuery.GetById(userId)
        }

        init {
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
        }
    }

    /**
     * 모든 사용자 조회 요청
     */
    class GetAll
} 