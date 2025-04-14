package kr.hhplus.be.server.controller.order

import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.OrderItemRequest
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

@RestController()
class OrderController(
    private val orderFacade: OrderFacade
) : OrderControllerApi {
    /**
     * 주문을 생성합니다.
     * 
     * @param req 주문 생성 요청 DTO
     * @return 생성된 주문 정보
     */
    override fun createOrder(
        @RequestBody req: OrderRequest.Create
    ): BaseResponse<OrderResponse.Order> {
        // 주문 항목 변환
        val orderItems = req.orderItemList.map { item ->
            OrderItemRequest(
                productId = item.productId,
                amount = item.count
            )
        }
        
        // 주문 생성
        val result = orderFacade.createOrder(
            userId = req.userId,
            orderItems = orderItems,
            couponUserId = req.payment.couponId
        )
        
        return BaseResponse.success(OrderResponse.Order.from(result))
    }
}