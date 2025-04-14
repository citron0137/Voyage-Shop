package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.DiscountType
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderDiscount
import kr.hhplus.be.server.domain.order.OrderItem
import java.time.LocalDateTime

/**
 * 주문 관련 응답 클래스
 */
sealed class OrderResult {
    /**
     * 단일 주문 조회 결과
     */
    data class Get(
        val orderId: String,
        val userId: String,
        val paymentId: String,
        val totalAmount: Long,
        val totalDiscountAmount: Long,
        val finalAmount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val items: kotlin.collections.List<OrderItem> = emptyList(),
        val discounts: kotlin.collections.List<OrderDiscount> = emptyList()
    ) : OrderResult() {
        companion object {
            /**
             * Order 도메인 객체로부터 Get DTO를 생성합니다.
             */
            fun from(order: Order): Get {
                return Get(
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
             * Order와 별도의 items, discounts로부터 Get DTO를 생성합니다.
             */
            fun from(
                order: Order,
                items: kotlin.collections.List<kr.hhplus.be.server.domain.order.OrderItem>,
                discounts: kotlin.collections.List<kr.hhplus.be.server.domain.order.OrderDiscount>
            ): Get {
                return Get(
                    orderId = order.orderId,
                    userId = order.userId,
                    paymentId = order.paymentId,
                    totalAmount = order.totalAmount,
                    totalDiscountAmount = order.totalDiscountAmount,
                    finalAmount = order.finalAmount,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt,
                    items = items.map { OrderItem.from(it) },
                    discounts = discounts.map { OrderDiscount.from(it) }
                )
            }
        }
    }

    /**
     * 주문 항목 결과 DTO
     */
    data class OrderItem(
        val orderItemId: String,
        val orderId: String,
        val productId: String,
        val amount: Long,
        val unitPrice: Long,
        val totalPrice: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : OrderResult() {
        companion object {
            /**
             * OrderItem 도메인 객체를 OrderResult.OrderItem DTO로 변환합니다.
             */
            fun from(orderItem: kr.hhplus.be.server.domain.order.OrderItem): OrderItem {
                return OrderItem(
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
    data class OrderDiscount(
        val orderDiscountId: String,
        val orderId: String,
        val discountType: DiscountType,
        val discountId: String,
        val discountAmount: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : OrderResult() {
        companion object {
            /**
             * OrderDiscount 도메인 객체를 OrderResult.OrderDiscount DTO로 변환합니다.
             */
            fun from(orderDiscount: kr.hhplus.be.server.domain.order.OrderDiscount): OrderDiscount {
                return OrderDiscount(
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
     * 주문 목록 조회 결과
     */
    data class Orders(
        val orders: kotlin.collections.List<Get>
    ) : OrderResult() {
        companion object {
            /**
             * Order 도메인 객체 목록으로부터 Orders DTO를 생성합니다.
             */
            fun from(orders: kotlin.collections.List<Order>): Orders {
                return Orders(
                    orders = orders.map { Get.from(it) }
                )
            }
            
            /**
             * Order 목록과 각 주문의 항목, 할인 정보로부터 Orders DTO를 생성합니다.
             */
            fun fromWithDetails(
                orders: kotlin.collections.List<Order>,
                itemsByOrderId: Map<String, kotlin.collections.List<kr.hhplus.be.server.domain.order.OrderItem>>,
                discountsByOrderId: Map<String, kotlin.collections.List<kr.hhplus.be.server.domain.order.OrderDiscount>>
            ): Orders {
                return Orders(
                    orders = orders.map { order ->
                        Get.from(
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