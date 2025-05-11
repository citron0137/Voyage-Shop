package kr.hhplus.be.server.domain.orderitemrank

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 주문 상품 순위 명령 관련 클래스
 */
sealed class OrderItemRankCommand {
    data class ReflectNewOrder(
        val createdAt: LocalDateTime,
        val orderItems: List<OrderItem>

    ){
        data class OrderItem(
            val productId: String,
            val orderCount: Long
        )
    }

    data class DeleteRanks(
        val type: OrderItemRankType,
        val date: LocalDate,
    )
}