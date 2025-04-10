package kr.hhplus.be.server.domain.userpoint

class UserPointCommand {

    class Create (
        val userId: String,
    ) {
        init {
            if (userId.isBlank()) throw UserPointException.UserIdShouldNotBlank("userId는 비어있을 수 없습니다.")
        }
    }

    class Charge (
        val userId: String,
        val amount: Long,
    ) {
        init {
            if (userId.isBlank()) throw UserPointException.UserIdShouldNotBlank("userId는 비어있을 수 없습니다.")
            if (amount <= 0) throw UserPointException.ChargeAmountShouldMoreThan0("충전량은 0보다 커야합니다.")
        }
    }

    class Use (
        val userId: String,
        val amount: Long,
    ) {
        init {
            if (userId.isBlank()) throw UserPointException.UserIdShouldNotBlank("userId는 비어있을 수 없습니다.")
            if (amount <= 0) throw UserPointException.UseAmountShouldMoreThan0("사용량은 0보다 커야합니다.")
        }
    }

}