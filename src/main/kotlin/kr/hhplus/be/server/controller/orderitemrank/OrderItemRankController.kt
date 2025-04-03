package kr.hhplus.be.server.controller.orderitemrank

import kr.hhplus.be.server.controller.orderitemrank.response.OrderItemRankResponseDTO
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController()
class OrderItemRankController {
    @GetMapping("/order-item-rank")
    fun getOrderItemRank(): BaseResponse<List<OrderItemRankResponseDTO>>{
        return BaseResponse.success(
            listOf(
                OrderItemRankResponseDTO(
                    productId= UUID.randomUUID().toString(),
                )
            )
        )
    }
}