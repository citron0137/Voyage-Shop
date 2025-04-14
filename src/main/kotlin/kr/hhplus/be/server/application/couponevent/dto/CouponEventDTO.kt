package kr.hhplus.be.server.application.couponevent.dto

data class CouponEventDTO(
    val id: String,
    val benefitMethod: String,
    val benefitAmount: String,
    val totalIssueAmount: Long,
    val leftIssueAmount: Long
)

data class CreateCouponEventCriteria(
    val benefitMethod: String,
    val benefitAmount: String,
    val totalIssueAmount: Long,
)

data class IssueCouponCriteria(
    val userId: String
)

data class IssuedCouponDTO(
    val couponUserId: String
) 