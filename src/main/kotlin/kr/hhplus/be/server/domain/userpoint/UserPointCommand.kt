package kr.hhplus.be.server.domain.userpoint

/**
 * 사용자 포인트 도메인 명령 관련 클래스
 */
sealed class UserPointCommand {
    /**
     * 사용자 포인트 생성 명령
     */
    data class Create(
        val userId: String,
    ) : UserPointCommand() {
        init {
            if (userId.isBlank()) throw UserPointException.UserIdShouldNotBlank("userId는 비어있을 수 없습니다.")
        }
    }

    /**
     * 사용자 포인트 충전 명령
     */
    data class Charge(
        val userId: String,
        val amount: Long,
    ) : UserPointCommand() {
        init {
            if (userId.isBlank()) throw UserPointException.UserIdShouldNotBlank("userId는 비어있을 수 없습니다.")
            if (amount <= 0) throw UserPointException.ChargeAmountShouldMoreThan0("충전량은 0보다 커야합니다.")
        }
    }

    /**
     * 사용자 포인트 사용 명령
     */
    data class Use(
        val userId: String,
        val amount: Long,
    ) : UserPointCommand() {
        init {
            if (userId.isBlank()) throw UserPointException.UserIdShouldNotBlank("userId는 비어있을 수 없습니다.")
            if (amount <= 0) throw UserPointException.UseAmountShouldMoreThan0("사용량은 0보다 커야합니다.")
        }
    }
}