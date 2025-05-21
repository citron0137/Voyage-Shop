package kr.hhplus.be.server.domain.orderitemrank

import java.time.LocalDate

/** OrderItemRankType **/
enum class OrderItemRankType {
    ONE_DAY,
    THREE_DAY,
    ONE_WEEK
}

/**
 * 주문 상품 순위 개별 아이템
 * 상품별 주문량에 따른 인기 순위를 표현합니다.
 */
data class OrderItemRank(
    val type: OrderItemRankType,
    val startedAt: LocalDate,
    val productId: String,
    val orderCount: Long,
)
