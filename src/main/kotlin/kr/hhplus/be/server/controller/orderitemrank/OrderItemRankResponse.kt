package kr.hhplus.be.server.controller.orderitemrank

/**
 * 주문 상품 순위 응답 관련 클래스
 */
class OrderItemRankResponse {
    /**
     * 주문 상품 순위 정보
     */
    data class Rank(
        val productId: String,
        val orderCount: Long = 0
    )
    
    /**
     * 주문 상품 순위 목록
     */
    data class RankList(
        val items: kotlin.collections.List<Rank>
    )
} 