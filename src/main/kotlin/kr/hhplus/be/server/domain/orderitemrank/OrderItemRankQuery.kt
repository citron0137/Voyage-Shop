package kr.hhplus.be.server.domain.orderitemrank

import java.time.LocalDate

/**
 * 주문 상품 순위 조회 관련 클래스
 */
sealed class OrderItemRankQuery {
    data class GetTopRanks(
        val type: OrderItemRankType,
        val date: LocalDate,
        val limit: Long = 5
    ) : OrderItemRankQuery()
}