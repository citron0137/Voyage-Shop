package kr.hhplus.be.server.domain.orderitemrank

import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 주문 상품 순위 서비스
 * 상품별 주문량 순위에 관한 비즈니스 로직을 제공합니다.
 */
@Service
class OrderItemRankService(
    private val repository: OrderItemRankRepository,
){

    fun getTopOrderItemRank(query: OrderItemRankQuery.GetTopRanks): List<OrderItemRank> {
        return this.repository.getRanks(
            rankType = query.type,
            startedAt = query.date,
            limit = query.limit
        )
    }

    fun reflectNewOrder(command: OrderItemRankCommand.ReflectNewOrder) {
        val now = LocalDateTime.now()

        // 1일짜리
        command.orderItems.forEach {
            this.repository.addOrderCount(
                rankType = OrderItemRankType.ONE_DAY,
                startedAt = now.toLocalDate(),
                productId = it.productId,
                orderCount = it.orderCount
            )
        }

        // 3일짜리 3개
        for(i in 0L..2L){
            command.orderItems.forEach {
                this.repository.addOrderCount(
                    rankType = OrderItemRankType.THREE_DAY,
                    startedAt = now.toLocalDate().minusDays(i),
                    productId = it.productId,
                    orderCount = it.orderCount
                )
            }

        }

        // 일주일짜리 1개
        val monday = now.toLocalDate().minusDays(now.dayOfWeek.value.toLong()+1)
        command.orderItems.forEach {
            this.repository.addOrderCount(
                rankType = OrderItemRankType.ONE_WEEK,
                startedAt = monday,
                productId = it.productId,
                orderCount = it.orderCount
            )
        }
    }

    fun deleteRank(command: OrderItemRankCommand.DeleteRanks){
        this.repository.deleteRanks(
            rankType = command.type,
            startedAt = command.date,
        )
    }
}