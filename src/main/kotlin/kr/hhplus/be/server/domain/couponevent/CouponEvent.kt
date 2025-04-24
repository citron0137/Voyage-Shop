package kr.hhplus.be.server.domain.couponevent

import java.time.LocalDateTime
import java.util.UUID

data class CouponEvent(
    val id: String = UUID.randomUUID().toString(),
    val benefitMethod: CouponEventBenefitMethod,
    val benefitAmount: String,
    val totalIssueAmount: Long,
    val leftIssueAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 쿠폰 이벤트의 남은 발급 수량을 감소시킵니다.
     * 재고가 부족한 경우 예외가 발생합니다.
     * 
     * @throws CouponEventException.OutOfStock 재고가 없는 경우 발생
     * @return 재고가 감소된 새로운 CouponEvent 객체
     */
    fun decreaseLeftIssueAmount(): CouponEvent {
        if (leftIssueAmount <= 0) {
            throw CouponEventException.OutOfStock("Coupon event $id is out of stock")
        }
        return this.copy(
            leftIssueAmount = leftIssueAmount - 1,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 쿠폰 발급이 가능한지 확인합니다.
     * @throws CouponEventException.OutOfStock 재고가 없는 경우 예외 발생
     */
    fun validateCanIssue() {
        if (leftIssueAmount <= 0) {
            throw CouponEventException.OutOfStock("Coupon event $id is out of stock")
        }
    }

    /**
     * 쿠폰 발급이 가능한지 확인합니다.
     * @return 발급 가능 여부
     */
    fun canIssue(): Boolean {
        return leftIssueAmount > 0
    }
} 