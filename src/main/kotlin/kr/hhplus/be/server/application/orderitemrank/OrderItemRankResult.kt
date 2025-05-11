package kr.hhplus.be.server.application.orderitemrank

import kr.hhplus.be.server.domain.orderitemrank.OrderItemRank

/**
 * 주문 아이템 순위 관련 응답 클래스
 */
class OrderItemRankResult {
    /**
     * 단일 주문 아이템 순위 결과
     */
    data class Single(
        val productId: String,
        val orderCount: Long
    ) {
        companion object {
            /**
             * 주문 아이템 순위 정보로 Single 객체를 생성합니다.
             */
            fun from(productId: String, orderCount: Long): Single {
                return Single(productId = productId, orderCount = orderCount)
            }
        }
    }
    

    data class Rank(
        val ranks: kotlin.collections.List<Single>,
        val period: Int,
        val limit: Int,
    ){
        companion object {
            fun from(rank: List<OrderItemRank>, period: Int, limit: Int): Rank {
                return Rank(
                    ranks = rank.map { Single(it.productId, it.orderCount) },
                    period = period,
                    limit = limit
                )
            }
        }
    }
}