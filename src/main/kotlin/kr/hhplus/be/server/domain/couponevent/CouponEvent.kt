package kr.hhplus.be.server.domain.couponevent

import java.time.LocalDateTime
import java.util.UUID

data class CouponEvent(
    val id: String = UUID.randomUUID().toString(),
    val benefitMethod: BenefitMethod,
    val benefitAmount: String,
    val totalIssueAmount: Int,
    var leftIssueAmount: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 쿠폰 이벤트의 남은 발급 수량을 감소시킵니다.
     * 재고가 부족한 경우 예외가 발생합니다.
     * 
     * @throws CEStockEmptyException 재고가 없는 경우 발생
     */
    fun decreaseLeftIssueAmount() {
        if (leftIssueAmount <= 0) {
            throw CEStockEmptyException(id)
        }
        leftIssueAmount--
        updatedAt = LocalDateTime.now()
    }

    /**
     * 쿠폰 발급이 가능한지 확인합니다.
     * @throws CEStockEmptyException 재고가 없는 경우 예외 발생
     */
    fun validateCanIssue() {
        if (leftIssueAmount <= 0) {
            throw CEStockEmptyException(id)
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