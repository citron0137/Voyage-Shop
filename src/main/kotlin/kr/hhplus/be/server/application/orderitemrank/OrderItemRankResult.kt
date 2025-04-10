package kr.hhplus.be.server.application.orderitemrank

/**
 * 주문 아이템 순위 결과 DTO
 * 파사드에서 컨트롤러로 데이터를 전달하기 위한 모델입니다.
 */
data class OrderItemRankResult(
    val productId: String,
    val orderCount: Long
) 