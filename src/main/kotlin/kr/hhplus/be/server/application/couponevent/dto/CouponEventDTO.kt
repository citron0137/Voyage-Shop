package kr.hhplus.be.server.application.couponevent.dto

data class CouponEventDTO(
    val id: String,
    val benefitMethod: String,
    val benefitAmount: String,
    val totalIssueAmount: Int,
    val leftIssueAmount: Int
)

data class CreateCouponEventCriteria(
    val benefitMethod: String,
    val benefitAmount: String,
    val totalIssueAmount: Int
)

data class IssueCouponCriteria(
    val userId: String
)

data class IssuedCouponDTO(
    val couponUserId: String
) 