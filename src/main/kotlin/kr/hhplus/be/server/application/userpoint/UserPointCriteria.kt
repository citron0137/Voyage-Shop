package kr.hhplus.be.server.application.userpoint

import kr.hhplus.be.server.domain.userpoint.UserPointCommand
import kr.hhplus.be.server.domain.userpoint.UserPointQuery

/**
 * 사용자 포인트 관련 요청 기준을 담는 클래스
 */
class UserPointCriteria {
    /**
     * 사용자 포인트 조회 요청
     */
    data class GetByUserId(
        val userId: String
    ) {
        fun toQuery(): UserPointQuery.GetByUserId {
            return UserPointQuery.GetByUserId(userId)
        }

        init {
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
        }
    }

    /**
     * 사용자 포인트 충전 요청
     */
    data class Charge(
        val userId: String,
        val amount: Long
    ) {
        fun toCommand(): UserPointCommand.Charge {
            return UserPointCommand.Charge(userId = userId, amount = amount)
        }

        init {
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
            require(amount > 0) { "충전 금액은 0보다 커야 합니다." }
        }
    }
    
    /**
     * 사용자 포인트 사용 요청
     */
    data class Use(
        val userId: String,
        val amount: Long
    ) {
        fun toCommand(): UserPointCommand.Use {
            return UserPointCommand.Use(userId = userId, amount = amount)
        }

        init {
            require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다." }
            require(amount > 0) { "사용 금액은 0보다 커야 합니다." }
        }
    }
} 