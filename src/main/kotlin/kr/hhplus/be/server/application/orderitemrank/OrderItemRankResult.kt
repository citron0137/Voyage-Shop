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
    
    /**
     * 주문 아이템 순위 목록 결과
     */
    data class List(
        val ranks: kotlin.collections.List<Single>
    ) {
        companion object {
            /**
             * 개별 순위 항목들을 리스트로 변환합니다.
             */
            fun from(ranks: kotlin.collections.List<Single>): List {
                return List(ranks)
            }
        }
    }

    data class Rank(
        val ranks: kotlin.collections.List<Single>,
        val period: Int,
        val limit: Int,
    ){
        companion object {
            fun from(rank: OrderItemRank): Rank {
                return Rank(
                    ranks = rank.items.map { Single.from(productId = it.productId, orderCount = it.orderCount) },
                    period = rank.periodInDays,
                    limit = rank.limit
                )
            }
        }
    }
}