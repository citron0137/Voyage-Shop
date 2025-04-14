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
        val totalPrice: Long,
        val status: String,
        val items: List<OrderItemDTO>,
        val couponUserId: String?,
        val discountAmount: Long,
        val finalPrice: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : OrderResult() {
        /**
         * 주문 항목 응답 클래스
         */
        data class OrderItemDTO(
            val orderItemId: String,
            val productId: String,
            val amount: Long,
            val price: Long,
            val totalPrice: Long
        )

        companion object {
            /**
             * Order 도메인 객체로부터 Get DTO를 생성합니다.
             */
            fun from(order: Order): Get {
                return Get(
                    orderId = order.id,
                    userId = order.userId,
                    totalPrice = order.totalPrice,
                    status = order.status.name,
                    items = order.items.map { mapOrderItem(it) },
                    couponUserId = order.couponUserId,
                    discountAmount = order.discountAmount,
                    finalPrice = order.finalPrice,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt
                )
            }

            /**
             * OrderItem 도메인 객체로부터 OrderItemDTO를 생성합니다.
             */
            private fun mapOrderItem(item: OrderItem): OrderItemDTO {
                return OrderItemDTO(
                    orderItemId = item.id,
                    productId = item.productId,
                    amount = item.amount,
                    price = item.price,
                    totalPrice = item.totalPrice
                )
            }
            
            /**
             * Order와 별도의 items, discounts로부터 Get DTO를 생성합니다.
             */
            fun from(
                order: Order,
                items: kotlin.collections.List<OrderItem>,
                discounts: kotlin.collections.List<OrderDiscount>
            ): Get {
                return Get(
                    orderId = order.id,
                    userId = order.userId,
                    totalPrice = order.totalPrice,
                    status = order.status.name,
                    items = items.map { mapOrderItem(it) },
                    couponUserId = order.couponUserId,
                    discountAmount = order.discountAmount,
                    finalPrice = order.finalPrice,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt
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
    data class List(
        val orders: kotlin.collections.List<Get>
    ) : OrderResult() {
        companion object {
            /**
             * Order 도메인 객체 목록으로부터 List DTO를 생성합니다.
             */
            fun from(orders: kotlin.collections.List<Order>): List {
                return List(
                    orders = orders.map { Get.from(it) }
                )
            }
            
            /**
             * Order 목록과 각 주문의 항목, 할인 정보로부터 List DTO를 생성합니다.
             */
            fun fromWithDetails(
                orders: kotlin.collections.List<Order>,
                itemsByOrderId: Map<String, kotlin.collections.List<OrderItem>>,
                discountsByOrderId: Map<String, kotlin.collections.List<OrderDiscount>>
            ): List {
                return List(
                    orders = orders.map { order ->
                        Get.from(
                            order = order,
                            items = itemsByOrderId[order.id] ?: emptyList(),
                            discounts = discountsByOrderId[order.id] ?: emptyList()
                        )
                    }
                )
            }
        }
    }
} 