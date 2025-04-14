package kr.hhplus.be.server.controller.couponevent.response

data class CouponEventResponseDTO(
    val id: String,
    val benefitMethod: String,
    val benefitAmount: String,
    val totalIssueAmount: Long,
    val leftIssueAmount: Long
)