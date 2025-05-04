package kr.hhplus.be.server.domain.orderitemrank

/**
 * 주문 상품 순위 조회 관련 클래스
 */
sealed class OrderItemRankQuery {
    /**
     * 최근 N일간의 상위 M개 주문 아이템 순위 조회
     */
    data class GetTopRanks(
        val days: Int = 3,
        val limit: Int = 5
    ) : OrderItemRankQuery() {
        init {
            require(days > 0) { "days must be positive" }
            require(limit > 0) { "limit must be positive" }
        }
    }
    
    /**
     * 최근 N일간의 상위 M개 주문 아이템 순위 조회 (단일 객체)
     */
    data class GetTopRank(
        val days: Int = 3,
        val limit: Int = 5
    ) : OrderItemRankQuery() {
        init {
            require(days > 0) { "days must be positive" }
            require(limit > 0) { "limit must be positive" }
        }
    }
} 