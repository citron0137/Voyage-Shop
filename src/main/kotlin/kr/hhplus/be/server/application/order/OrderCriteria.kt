package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.controller.order.OrderRequestDTO

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
        init {
            require(orderId.isNotBlank()) { "orderId must not be blank" }
        }
    }

    /**
     * 사용자 ID로 주문을 조회하는 기준
     */
    data class GetByUserId(
        val userId: String
    ) : OrderCriteria() {
        init {
            require(userId.isNotBlank()) { "userId must not be blank" }
        }
    }

    /**
     * 모든 주문을 조회하는 기준
     */
    object GetAll : OrderCriteria()

    /**
     * 주문을 생성하는 기준
     */
    data class Create(
        val userId: String,
        val items: List<OrderItem>
    ) : OrderCriteria() {
        init {
            require(userId.isNotBlank()) { "userId must not be blank" }
            require(items.isNotEmpty()) { "items must not be empty" }
        }

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
        }

        companion object {
            fun from(request: OrderRequestDTO.CreateOrderRequest): Create {
                return Create(
                    userId = request.userId,
                    items = request.items.map {
                        OrderItem(
                            productId = it.productId,
                            amount = it.amount
                        )
                    }
                )
            }
        }
    }
} 