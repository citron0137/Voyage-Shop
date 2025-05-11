package kr.hhplus.be.server.controller.orderitemrank

import kr.hhplus.be.server.application.orderitemrank.OrderItemRankCriteria
import kr.hhplus.be.server.application.orderitemrank.OrderItemRankApplication
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

@RestController()
class OrderItemRankController(
    private val orderItemRankApplication: OrderItemRankApplication
) : OrderItemRankControllerApi {
    override fun getOrderItemRank(): BaseResponse<List<OrderItemRankResponse.Rank>> {
        // 파사드에서 순위 결과 얻기
        val result = orderItemRankApplication.getOrderItemRanksInThreeDay()
        
        // 파사드 결과를 컨트롤러 응답 DTO로 변환 (주문 수량 기준 내림차순 정렬 보장)
        val responseItems = result.ranks
            .map { OrderItemRankResponse.Rank.from(it) }
            .sortedByDescending { it.orderCount }

        return BaseResponse.success(responseItems)
    }
    
    override fun resetOrderItemRank(): BaseResponse<Unit> {
        // 파사드를 통해 orderItemRank 초기화
        orderItemRankApplication.resetOrderItemRanks()
        return BaseResponse.success(Unit)
    }
}