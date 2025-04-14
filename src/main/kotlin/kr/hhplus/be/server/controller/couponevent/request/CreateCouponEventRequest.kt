package kr.hhplus.be.server.controller.couponevent.request

data class CreateCouponEventRequest(
    val benefitMethod: String,
    val benefitAmount: String,
    val totalIssueAmount: Long
)

