package kr.hhplus.be.server.controller.orderitemrank

import kr.hhplus.be.server.application.orderitemrank.OrderItemRankCriteria
import kr.hhplus.be.server.application.orderitemrank.OrderItemRankFacade
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

@RestController()
class OrderItemRankController(
    private val orderItemRankFacade: OrderItemRankFacade
) : OrderItemRankControllerApi {
    override fun getOrderItemRank(): BaseResponse<List<OrderItemRankResponse.Rank>> {
        // 파사드에서 순위 결과 얻기
        val criteria = OrderItemRankCriteria.RecentTopRanks()
        val result = orderItemRankFacade.getRecentTopOrderItemRanks(criteria)
        
        // 파사드 결과를 컨트롤러 응답 DTO로 변환
        val responseItems = result.ranks.map { OrderItemRankResponse.Rank.from(it) }
        
        return BaseResponse.success(responseItems)
    }
}