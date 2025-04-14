package kr.hhplus.be.server.domain.couponevent

import java.time.LocalDateTime
import java.util.UUID

/**
 * 쿠폰 이벤트 생성 명령
 */
data class CreateCouponEventCommand(
    val benefitMethod: BenefitMethod,
    val benefitAmount: String,
    val totalIssueAmount: Long
) {
    fun toEntity(): CouponEvent {
        val now = LocalDateTime.now()
        return CouponEvent(
            id = UUID.randomUUID().toString(),
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            totalIssueAmount = totalIssueAmount,
            leftIssueAmount = totalIssueAmount,
            createdAt = now,
            updatedAt = now
        )
    }
} 