package kr.hhplus.be.server.domain.orderitemrank

/**
 * 주문 상품 순위 명령 관련 클래스
 */
sealed class OrderItemRankCommand {
    /**
     * 최근 N일간의 상위 M개 주문 아이템 순위 저장 명령
     */
    data class SaveTopRank(
        val ranks: List<RankItem>,
        val days: Int = 3,
        val limit: Int = 5
    ) : OrderItemRankCommand() {
        init {
            require(days > 0) { "days must be positive" }
            require(limit > 0) { "limit must be positive" }
        }
        
        /**
         * 명령에서 사용하는 간소화된 랭킹 아이템
         */
        data class RankItem(
            val productId: String,
            val orderCount: Long
        )

        fun toDomain(): OrderItemRank {
            return OrderItemRank.create(
                items = ranks.map { rankItem ->
                    OrderItemRank.Item(productId = rankItem.productId, orderCount = rankItem.orderCount, rank = 0)
                },
                days = days,
                limit = limit
            )
        }
    }
} 