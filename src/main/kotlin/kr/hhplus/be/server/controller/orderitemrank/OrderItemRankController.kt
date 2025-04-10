package kr.hhplus.be.server.controller.orderitemrank

import kr.hhplus.be.server.application.orderitemrank.OrderItemRankFacade
import kr.hhplus.be.server.controller.orderitemrank.response.OrderItemRankResponseDTO
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

@RestController()
class OrderItemRankController(
    private val orderItemRankFacade: OrderItemRankFacade
) {
    @GetMapping("/order-item-rank")
    fun getOrderItemRank(): BaseResponse<List<OrderItemRankResponseDTO>> {
        // 파사드에서 순위 결과 얻기
        val topRankedItems = orderItemRankFacade.getRecentTopOrderItemRanks()
        
        // 파사드 결과를 컨트롤러 응답 DTO로 변환
        val responseItems = topRankedItems.map { rankResult ->
            OrderItemRankResponseDTO(
                productId = rankResult.productId,
                orderCount = rankResult.orderCount
            )
        }
        
        return BaseResponse.success(responseItems)
    }
}