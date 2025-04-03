package kr.hhplus.be.server.controller.order.request

class CreateOrderRequestPayment (
    val couponId: String?,
)

class CreateOrderRequestOrderItem(
    val productId: String,
    val count: Long,
)

class CreateOrderRequest (
    val orderItemList: List<CreateOrderRequestOrderItem>,
    val payment: CreateOrderRequestPayment
)
