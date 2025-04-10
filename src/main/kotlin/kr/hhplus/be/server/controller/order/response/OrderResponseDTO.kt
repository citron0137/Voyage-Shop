package kr.hhplus.be.server.controller.order.response

import kr.hhplus.be.server.application.order.OrderResult
import java.time.LocalDateTime

/**
 * 주문 응답 DTO
 */
data class OrderResponseDTO(
    val id: String,
    val userId: String? = null,
    val paymentId: String? = null,
    val totalAmount: Long? = null,
    val totalDiscountAmount: Long? = null,
    val finalAmount: Long? = null,
    val createdAt: LocalDateTime? = null,
    val items: List<OrderItemResponseDTO>? = null,
    val discounts: List<OrderDiscountResponseDTO>? = null
) {
    companion object {
        /**
         * OrderResult DTO를 OrderResponseDTO로 변환합니다.
         */
        fun from(orderResult: OrderResult): OrderResponseDTO {
            return OrderResponseDTO(
                id = orderResult.orderId,
                userId = orderResult.userId,
                paymentId = orderResult.paymentId,
                totalAmount = orderResult.totalAmount,
                totalDiscountAmount = orderResult.totalDiscountAmount,
                finalAmount = orderResult.finalAmount,
                createdAt = orderResult.createdAt,
                items = orderResult.items.map { OrderItemResponseDTO.from(it) },
                discounts = orderResult.discounts.map { OrderDiscountResponseDTO.from(it) }
            )
        }
    }
}

/**
 * 주문 항목 응답 DTO
 */
data class OrderItemResponseDTO(
    val id: String,
    val productId: String,
    val amount: Long,
    val unitPrice: Long,
    val totalPrice: Long
) {
    companion object {
        /**
         * OrderItemResult DTO를 OrderItemResponseDTO로 변환합니다.
         */
        fun from(orderItemResult: kr.hhplus.be.server.application.order.OrderItemResult): OrderItemResponseDTO {
            return OrderItemResponseDTO(
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
 * 주문 할인 응답 DTO
 */
data class OrderDiscountResponseDTO(
    val id: String,
    val type: String,
    val discountId: String,
    val amount: Long
) {
    companion object {
        /**
         * OrderDiscountResult DTO를 OrderDiscountResponseDTO로 변환합니다.
         */
        fun from(orderDiscountResult: kr.hhplus.be.server.application.order.OrderDiscountResult): OrderDiscountResponseDTO {
            return OrderDiscountResponseDTO(
                id = orderDiscountResult.orderDiscountId,
                type = orderDiscountResult.discountType.name,
                discountId = orderDiscountResult.discountId,
                amount = orderDiscountResult.discountAmount
            )
        }
    }
}
