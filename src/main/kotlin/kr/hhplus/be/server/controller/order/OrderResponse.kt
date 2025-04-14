package kr.hhplus.be.server.controller.order

import kr.hhplus.be.server.application.order.OrderResult
import java.time.LocalDateTime

/**
 * 주문 응답 관련 클래스
 */
class OrderResponse {
    /**
     * 주문 정보 응답
     */
    data class Order(
        val id: String,
        val userId: String? = null,
        val paymentId: String? = null,
        val totalAmount: Long? = null,
        val totalDiscountAmount: Long? = null,
        val finalAmount: Long? = null,
        val createdAt: LocalDateTime? = null,
        val items: List<OrderItem>? = null,
        val discounts: List<OrderDiscount>? = null
    ) {
        companion object {
            /**
             * OrderResult DTO를 Order로 변환합니다.
             */
            fun from(orderResult: OrderResult): Order {
                return Order(
                    id = orderResult.orderId,
                    userId = orderResult.userId,
                    paymentId = orderResult.paymentId,
                    totalAmount = orderResult.totalAmount,
                    totalDiscountAmount = orderResult.totalDiscountAmount,
                    finalAmount = orderResult.finalAmount,
                    createdAt = orderResult.createdAt,
                    items = orderResult.items.map { OrderItem.from(it) },
                    discounts = orderResult.discounts.map { OrderDiscount.from(it) }
                )
            }
        }
    }
    
    /**
     * 주문 항목 응답
     */
    data class OrderItem(
        val id: String,
        val productId: String,
        val amount: Long,
        val unitPrice: Long,
        val totalPrice: Long
    ) {
        companion object {
            /**
             * OrderItemResult를 OrderItem으로 변환합니다.
             */
            fun from(orderItemResult: kr.hhplus.be.server.application.order.OrderItemResult): OrderItem {
                return OrderItem(
                    id = orderItemResult.orderItemId,
                    productId = orderItemResult.productId,
                    amount = orderItemResult.amount,
                    unitPrice = orderItemResult.unitPrice,
                    totalPrice = orderItemResult.totalPrice
                )
            }
        }
    }
    
    /**
     * 주문 할인 응답
     */
    data class OrderDiscount(
        val id: String,
        val type: String,
        val discountId: String,
        val amount: Long
    ) {
        companion object {
            /**
             * OrderDiscountResult를 OrderDiscount로 변환합니다.
             */
            fun from(orderDiscountResult: kr.hhplus.be.server.application.order.OrderDiscountResult): OrderDiscount {
                return OrderDiscount(
                    id = orderDiscountResult.orderDiscountId,
                    type = orderDiscountResult.discountType.name,
                    discountId = orderDiscountResult.discountId,
                    amount = orderDiscountResult.discountAmount
                )
            }
        }
    }
    
    /**
     * 주문 목록 응답
     */
    data class OrderList(
        val items: kotlin.collections.List<Order>
    )
} 