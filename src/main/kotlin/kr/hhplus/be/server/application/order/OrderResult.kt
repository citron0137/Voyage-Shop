package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.DiscountType
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderDiscount
import kr.hhplus.be.server.domain.order.OrderItem
import java.time.LocalDateTime

/**
 * 주문 결과 DTO
 */
data class OrderResult(
    val orderId: String,
    val userId: String,
    val paymentId: String,
    val totalAmount: Long,
    val totalDiscountAmount: Long,
    val finalAmount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val items: List<OrderItemResult> = emptyList(),
    val discounts: List<OrderDiscountResult> = emptyList()
) {
    companion object {
        /**
         * Order 도메인 객체와 관련 항목을 OrderResult DTO로 변환합니다.
         */
        fun from(
            order: Order,
            items: List<OrderItem> = emptyList(),
            discounts: List<OrderDiscount> = emptyList()
        ): OrderResult {
            return OrderResult(
                orderId = order.orderId,
                userId = order.userId,
                paymentId = order.paymentId,
                totalAmount = order.totalAmount,
                totalDiscountAmount = order.totalDiscountAmount,
                finalAmount = order.finalAmount,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                items = items.map { OrderItemResult.from(it) },
                discounts = discounts.map { OrderDiscountResult.from(it) }
            )
        }
    }
}

/**
 * 주문 항목 결과 DTO
 */
data class OrderItemResult(
    val orderItemId: String,
    val orderId: String,
    val productId: String,
    val amount: Long,
    val unitPrice: Long,
    val totalPrice: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * OrderItem 도메인 객체를 OrderItemResult DTO로 변환합니다.
         */
        fun from(orderItem: OrderItem): OrderItemResult {
            return OrderItemResult(
                orderItemId = orderItem.orderItemId,
                orderId = orderItem.orderId,
                productId = orderItem.productId,
                amount = orderItem.amount,
                unitPrice = orderItem.unitPrice,
                totalPrice = orderItem.totalPrice,
                createdAt = orderItem.createdAt,
                updatedAt = orderItem.updatedAt
            )
        }
    }
}

/**
 * 주문 할인 결과 DTO
 */
data class OrderDiscountResult(
    val orderDiscountId: String,
    val orderId: String,
    val discountType: DiscountType,
    val discountId: String,
    val discountAmount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * OrderDiscount 도메인 객체를 OrderDiscountResult DTO로 변환합니다.
         */
        fun from(orderDiscount: OrderDiscount): OrderDiscountResult {
            return OrderDiscountResult(
                orderDiscountId = orderDiscount.orderDiscountId,
                orderId = orderDiscount.orderId,
                discountType = orderDiscount.discountType,
                discountId = orderDiscount.discountId,
                discountAmount = orderDiscount.discountAmount,
                createdAt = orderDiscount.createdAt,
                updatedAt = orderDiscount.updatedAt
            )
        }
    }
}

/**
 * 주문 목록 결과 DTO
 */
data class OrderListResult(
    val orders: List<OrderResult>
) {
    companion object {
        /**
         * Order 도메인 객체 목록을 OrderListResult DTO로 변환합니다.
         */
        fun from(orders: List<Order>): OrderListResult {
            return OrderListResult(
                orders = orders.map { OrderResult.from(it) }
            )
        }
        
        /**
         * 주문과 관련 항목을 포함하여 OrderListResult DTO로 변환합니다.
         */
        fun fromWithDetails(
            orders: List<Order>,
            itemsByOrderId: Map<String, List<OrderItem>>,
            discountsByOrderId: Map<String, List<OrderDiscount>>
        ): OrderListResult {
            return OrderListResult(
                orders = orders.map { order ->
                    OrderResult.from(
                        order = order,
                        items = itemsByOrderId[order.orderId] ?: emptyList(),
                        discounts = discountsByOrderId[order.orderId] ?: emptyList()
                    )
                }
            )
        }
    }
} 