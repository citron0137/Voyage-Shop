package kr.hhplus.be.server.application.orderitemrank

import kr.hhplus.be.server.domain.order.OrderItemCommand
import kr.hhplus.be.server.domain.order.OrderService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 주문 아이템 순위 파사드
 * 주문 아이템 순위 관련 비즈니스 로직을 캡슐화하고 컨트롤러에서 사용할 수 있는 단순한 인터페이스를 제공합니다.
 */
@Component
class OrderItemRankFacade(
    private val orderService: OrderService
) {
    /**
     * 최근 N일간의 주문 아이템 중 상위 M개 순위를 조회합니다.
     *
     * @param criteria 순위 조회 기준
     * @return 상위 M개 주문 아이템 순위 목록
     */
    @Transactional(readOnly = true)
    fun getRecentTopOrderItemRanks(criteria: OrderItemRankCriteria.RecentTopRanks = OrderItemRankCriteria.RecentTopRanks()): OrderItemRankResult.List {
        // 현재 시간으로부터 지정된 일수 전 계산
        val daysAgo = LocalDateTime.now().minusDays(criteria.days.toLong())
        
        // 모든 주문 조회 후 지정된 일수 이내 주문만 필터링
        val recentOrders = orderService.getAllOrders()
            .filter { it.createdAt.isAfter(daysAgo) }
            
        // 최근 주문의 아이템만 조회
        val recentOrderItems = recentOrders.flatMap { order ->
            orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId(order.orderId))
        }
        
        // 상품 ID별로 그룹화하여 주문 횟수를 계산하고 상위 M개만 추출
        val topRanks = recentOrderItems
            .groupBy { it.productId }
            .mapValues { it.value.sumOf { item -> item.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(criteria.limit)
            .map { OrderItemRankResult.Rank(productId = it.first, orderCount = it.second) }
            
        return OrderItemRankResult.List(topRanks)
    }
    
    /**
     * 최근 3일간의 주문 아이템 중 상위 5개 순위를 조회합니다. (호환성 메서드)
     */
    @Transactional(readOnly = true)
    fun getRecentTopOrderItemRanks(): List<OrderItemRankResult.Rank> {
        return getRecentTopOrderItemRanks(OrderItemRankCriteria.RecentTopRanks()).ranks
    }
} 