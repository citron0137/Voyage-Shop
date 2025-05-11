package kr.hhplus.be.server.domain.orderitemrank

import java.time.LocalDate


/**
 * 주문 상품 순위 리포지터리 인터페이스
 * 상품별 주문량에 따른 인기 순위를 관리합니다.
 */
interface OrderItemRankRepository {
    fun getRanks(
        rankType: OrderItemRankType,
        startedAt: LocalDate,
        limit: Long
    ): List<OrderItemRank>

    fun addOrderCount(
        rankType: OrderItemRankType,
        startedAt: LocalDate,
        productId: String,
        orderCount: Long,
    )

    fun deleteRanks(
        rankType: OrderItemRankType,
        startedAt: LocalDate,
    )
}