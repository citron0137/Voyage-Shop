package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderDiscount
import kr.hhplus.be.server.domain.order.OrderDiscountType
import kr.hhplus.be.server.domain.order.OrderItem
import java.time.LocalDateTime

/**
 * 주문 관련 응답 클래스
 */
sealed class OrderResult {
    /**
     * 단일 주문 조회 결과
     */
    data class Single(
        val orderId: String,
        val userId: String,
        val paymentId: String,
        val totalAmount: Long,
        val totalDiscountAmount: Long,
        val finalAmount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val items: kotlin.collections.List<OrderItemResult> = emptyList(),
        val discounts: kotlin.collections.List<OrderDiscountResult> = emptyList()
    ) : OrderResult() {
        companion object {
            /**
             * Order 도메인 객체로부터 Single DTO를 생성합니다.
             */
            fun from(order: Order): Single {
                return Single(
                    orderId = order.orderId,
                    userId = order.userId,
                    paymentId = order.paymentId,
                    totalAmount = order.totalAmount,
                    totalDiscountAmount = order.totalDiscountAmount,
                    finalAmount = order.finalAmount,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt
                )
            }
            
            /**
             * Order와 별도의 items, discounts로부터 Single DTO를 생성합니다.
             */
            fun from(
                order: Order,
                items: kotlin.collections.List<OrderItem>,
                discounts: kotlin.collections.List<OrderDiscount>
            ): Single {
                return Single(
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
     * 주문 목록 조회 결과
     */
    data class List(
        val orders: kotlin.collections.List<Single>
    ) : OrderResult() {
        companion object {
            /**
             * Order 도메인 객체 목록으로부터 Orders DTO를 생성합니다.
             */
            fun from(orders: kotlin.collections.List<Order>): List {
                return List(
                    orders = orders.map { Single.from(it) }
                )
            }
            
            /**
             * Order 목록과 각 주문의 항목, 할인 정보로부터 Orders DTO를 생성합니다.
             */
            fun fromWithDetails(
                orders: kotlin.collections.List<Order>,
                itemsByOrderId: Map<String, kotlin.collections.List<OrderItem>>,
                discountsByOrderId: Map<String, kotlin.collections.List<OrderDiscount>>
            ): List {
                return List(
                    orders = orders.map { order ->
                        Single.from(
                            order = order,
                            items = itemsByOrderId[order.orderId] ?: emptyList(),
                            discounts = discountsByOrderId[order.orderId] ?: emptyList()
                        )
                    }
                )
            }
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
         * OrderItem 도메인 객체를 OrderItem DTO로 변환합니다.
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
    val orderDiscountType: OrderDiscountType,
    val discountId: String,
    val discountAmount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * OrderDiscount 도메인 객체를 OrderDiscount DTO로 변환합니다.
         */
        fun from(orderDiscount: OrderDiscount): OrderDiscountResult {
            return OrderDiscountResult(
                orderDiscountId = orderDiscount.orderDiscountId,
                orderId = orderDiscount.orderId,
                orderDiscountType = orderDiscount.type,
                discountId = orderDiscount.discountId,
                discountAmount = orderDiscount.discountAmount,
                createdAt = orderDiscount.createdAt,
                updatedAt = orderDiscount.updatedAt
            )
        }
    }
} 