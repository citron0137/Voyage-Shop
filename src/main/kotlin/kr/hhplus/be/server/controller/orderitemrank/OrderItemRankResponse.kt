package kr.hhplus.be.server.controller.orderitemrank

import kr.hhplus.be.server.application.orderitemrank.OrderItemRankResult

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
    ) {
        companion object {
            fun from(result: OrderItemRankResult.Rank): Rank {
                return Rank(
                    productId = result.productId,
                    orderCount = result.orderCount
                )
            }
        }
    }
    
    /**
     * 주문 상품 순위 목록
     */
    data class RankList(
        val items: kotlin.collections.List<Rank>
    ) {
        companion object {
            fun from(result: OrderItemRankResult.List): RankList {
                return RankList(
                    items = result.ranks.map { Rank.from(it) }
                )
            }
        }
    }
} 