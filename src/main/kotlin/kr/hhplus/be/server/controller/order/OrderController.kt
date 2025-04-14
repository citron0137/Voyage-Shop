package kr.hhplus.be.server.controller.order

import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.application.order.OrderFacade
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
        // 주문 생성
        val criteria = OrderCriteria.Create(
            userId = req.userId,
            items = req.orderItemList.map {
                OrderCriteria.Create.OrderItem(
                    productId = it.productId,
                    amount = it.count
                )
            },
            couponUserId = req.payment.couponId
        )
        val result = orderFacade.createOrder(criteria)
        
        return BaseResponse.success(OrderResponse.Order.from(result))
    }
}