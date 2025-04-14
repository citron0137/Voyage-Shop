package kr.hhplus.be.server.application.orderitemrank

/**
 * 주문 아이템 순위 관련 응답 클래스
 */
sealed class OrderItemRankResult {
    /**
     * 단일 주문 아이템 순위 결과
     */
    data class Rank(
        val productId: String,
        val orderCount: Long
    ) : OrderItemRankResult()
    
    /**
     * 주문 아이템 순위 목록 결과
     */
    data class List(
        val ranks: kotlin.collections.List<Rank>
    ) : OrderItemRankResult() {
        companion object {
            /**
             * 개별 순위 항목들을 리스트로 변환합니다.
             */
            fun from(ranks: kotlin.collections.List<Rank>): List {
                return List(ranks)
            }
        }
    }
} 