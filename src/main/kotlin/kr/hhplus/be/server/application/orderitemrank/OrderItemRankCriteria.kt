package kr.hhplus.be.server.application.orderitemrank

import kr.hhplus.be.server.domain.order.OrderQuery

/**
 * 주문 아이템 순위 조회 관련 기준 클래스
 */
class OrderItemRankCriteria {
    /**
     * 최근 N일 동안의 상위 M개 주문 아이템 순위 조회 기준
     */
    data class RecentTopRanks(
        val days: Int = 3,
        val limit: Int = 5
    ) {
        fun toQuery(): OrderQuery.GetAll {
            return OrderQuery.GetAll
        }

        init {
            require(days > 0) { "days must be positive" }
            require(limit > 0) { "limit must be positive" }
        }
    }
} 