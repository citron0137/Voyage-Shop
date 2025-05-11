package kr.hhplus.be.server.application.orderitemrank

import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankCommand
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankQuery
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankService
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankType
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 주문 아이템 순위 애플리케이션 서비스
 * 여러 도메인 서비스를 조합하여 주문 아이템 순위 조회 및 캐싱 관리 등의 비즈니스 유스케이스를 구현합니다.
 */
@Component
class OrderItemRankApplication(
    private val orderItemRankService: OrderItemRankService,
) {
    fun getOrderItemRanksInThreeDay(): OrderItemRankResult.Rank {
        val result = orderItemRankService.getTopOrderItemRank(OrderItemRankQuery.GetTopRanks(
            OrderItemRankType.THREE_DAY, LocalDate.now().minusDays(2)
        ))
        return OrderItemRankResult.Rank.from(result, 3, 5)
    }

    fun resetOrderItemRanks() {
        orderItemRankService.deleteRank(
            OrderItemRankCommand.DeleteRanks(
                OrderItemRankType.THREE_DAY, LocalDate.now().minusDays(2)
            )
        )
    }
}
