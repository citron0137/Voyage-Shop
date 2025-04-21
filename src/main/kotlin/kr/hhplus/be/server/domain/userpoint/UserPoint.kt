package kr.hhplus.be.server.domain.userpoint

import java.time.LocalDateTime

data class UserPoint (
    val userPointId: String,
    val userId: String,
    val amount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 포인트를 충전합니다.
     *
     * @param amount 충전할 금액
     * @return 충전 후 새로운 UserPoint 객체
     */
    fun charge(amount: Long): UserPoint {
        return this.copy(
            amount = this.amount + amount,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 포인트를 사용합니다.
     *
     * @param amount 사용할 금액
     * @return 사용 후 새로운 UserPoint 객체
     */
    fun use(amount: Long): UserPoint {
        return this.copy(
            amount = this.amount - amount,
            updatedAt = LocalDateTime.now()
        )
    }
}