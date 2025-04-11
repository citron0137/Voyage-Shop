package kr.hhplus.be.server.domain.couponevent

/**
 * 쿠폰 이벤트 생성 명령
 * @param benefitMethod 혜택 방식 (고정 금액 할인 또는 퍼센트 할인)
 * @param benefitAmount 혜택 양 (금액 또는 퍼센트)
 * @param totalIssueAmount 총 발급 수량
 */
data class CreateCouponEventCommand(
    val benefitMethod: BenefitMethod,
    val benefitAmount: String,
    val totalIssueAmount: Int
) {
    fun toCouponEvent(): CouponEvent {
        return CouponEvent(
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            totalIssueAmount = totalIssueAmount,
            leftIssueAmount = totalIssueAmount
        )
    }
} 