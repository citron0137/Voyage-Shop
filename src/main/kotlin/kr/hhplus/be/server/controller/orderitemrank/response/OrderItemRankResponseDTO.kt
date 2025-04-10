package kr.hhplus.be.server.controller.orderitemrank.response

data class OrderItemRankResponseDTO(
    val productId: String,
    val orderCount: Long = 0
)
