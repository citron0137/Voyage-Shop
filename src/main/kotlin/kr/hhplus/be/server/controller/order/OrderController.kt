package kr.hhplus.be.server.controller.order

import kr.hhplus.be.server.controller.order.request.CreateOrderRequest
import kr.hhplus.be.server.controller.order.response.OrderResponseDTO
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController()
class OrderController {
    @PostMapping("/orders")
    fun createOrder(
        @RequestBody req: CreateOrderRequest,
    ): BaseResponse<OrderResponseDTO>{
        return BaseResponse.success(
            OrderResponseDTO(id=UUID.randomUUID().toString())
        )
    }
}