package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankCommand
import kr.hhplus.be.server.domain.payment.PaymentCommand
import kr.hhplus.be.server.domain.product.ProductException
import kr.hhplus.be.server.domain.product.ProductCommand
import kr.hhplus.be.server.domain.product.ProductQuery
import java.time.LocalDateTime

/**
 * 주문 관련 요청 기준을 담는 클래스
 */
sealed class OrderCriteria {
    /**
     * 주문 ID로 주문을 조회하는 기준
     */
    data class GetById(
        val orderId: String
    ) : OrderCriteria() {
        
        /**
         * 도메인 Query로 변환
         *
         * @return OrderQuery.GetById 객체
         */
        fun toQuery(): OrderQuery.GetById {
            return OrderQuery.GetById(orderId)
        }
        
        /**
         * 주문 항목 조회를 위한 Query로 변환
         *
         * @return OrderQuery.GetOrderItemsByOrderId 객체
         */
        fun toItemsQuery(): OrderQuery.GetOrderItemsByOrderId {
            return OrderQuery.GetOrderItemsByOrderId(orderId)
        }
        
        /**
         * 주문 할인 조회를 위한 Query로 변환
         *
         * @return OrderQuery.GetOrderDiscountsByOrderId 객체
         */
        fun toDiscountsQuery(): OrderQuery.GetOrderDiscountsByOrderId {
            return OrderQuery.GetOrderDiscountsByOrderId(orderId)
        }
    }

    /**
     * 사용자 ID로 주문을 조회하는 기준
     */
    data class GetByUserId(
        val userId: String
    ) : OrderCriteria() {
        
        /**
         * 도메인 Query로 변환
         *
         * @return OrderQuery.GetByUserId 객체
         */
        fun toQuery(): OrderQuery.GetByUserId {
            return OrderQuery.GetByUserId(userId)
        }
    }

    /**
     * 모든 주문을 조회하는 기준
     */
    object GetAll : OrderCriteria() {
        /**
         * 도메인 Query로 변환
         *
         * @return OrderQuery.GetAll 객체
         */
        fun toQuery(): OrderQuery.GetAll {
            return OrderQuery.GetAll
        }
    }

    /**
     * 주문을 생성하는 기준
     */
    data class Create(
        val userId: String,
        val items: List<OrderItem>,
        val couponUserId: String? = null
    ) : OrderCriteria() {

        /**
         * 주문 항목 생성 기준
         */
        data class OrderItem(
            val productId: String,
            val amount: Long
        ) {
            init {
                require(productId.isNotBlank()) { "productId must not be blank" }
                require(amount > 0) { "amount must be positive" }
            }
            
            /**
             * 상품 가격 정보가 포함된 도메인 Command로 변환
             *
             * @param unitPrice 상품 단가
             * @return OrderItemCommand.Create 객체
             */
            fun toCommand(unitPrice: Long): OrderItemCommand.Create {
                return OrderItemCommand.Create(
                    productId = productId,
                    amount = amount,
                    unitPrice = unitPrice
                )
            }
            fun toDecreaseStockAmountCommand(): ProductCommand.DecreaseStock {
                return ProductCommand.DecreaseStock(
                    productId = productId,
                    amount = amount
                )
            }
        }

        fun toDecreaseStockAmountCommands(): List<ProductCommand.DecreaseStock> {
            return items.map { it.toDecreaseStockAmountCommand() }
        }

        fun toCreateOrderCommand(
            paymentId: String,
            productPriceMap: Map<String, Long>,
            couponDiscountAmount: Long = 0
        ): OrderCommand.Create {
            return OrderCommand.Create(
                userId = userId,
                paymentId = paymentId,
                orderItems = items.map { 
                    it.toCommand(productPriceMap[it.productId] 
                    ?: throw ProductException.NotFound("상품을 찾을 수 없습니다.")) 
                },
                orderDiscounts = couponUserId?.let {
                    listOf(OrderDiscountCommand.Create(
                        orderDiscountType = OrderDiscountType.COUPON,
                        discountId = couponUserId,
                        discountAmount = couponDiscountAmount
                    ))
                } ?: emptyList()
            )
        }

        fun toGetProductQueries(): List<ProductQuery.GetById> {
            return this.items.map { ProductQuery.GetById(it.productId) }
        }

        fun toPaymentCommand(totalPrice: Long, discountPrice: Long): PaymentCommand.Create {
            return PaymentCommand.Create(this.userId, totalPrice - discountPrice)
        }

        fun toReflectNewOrder(createdAt: LocalDateTime): OrderItemRankCommand.ReflectNewOrder {
            return OrderItemRankCommand.ReflectNewOrder(
                createdAt,
                this.items.map {
                    OrderItemRankCommand.ReflectNewOrder.OrderItem(it.productId, it.amount)
                }
            )
        }
    }
} 