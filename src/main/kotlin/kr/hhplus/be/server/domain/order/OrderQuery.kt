package kr.hhplus.be.server.domain.order

/**
 * 주문 관련 조회 쿼리를 정의하는 sealed class
 */
sealed class OrderQuery {
    /**
     * 주문 ID로 주문 조회 쿼리
     */
    data class GetById(
        val orderId: String
    ) : OrderQuery() {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
    
    /**
     * 사용자 ID로 주문 목록 조회 쿼리
     */
    data class GetByUserId(
        val userId: String
    ) : OrderQuery() {
        init {
            if (userId.isBlank()) throw OrderException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
        }
    }
    
    /**
     * 모든 주문 조회 쿼리
     */
    object GetAll : OrderQuery()
    
    /**
     * 주문 ID로 주문 상품 목록 조회 쿼리
     */
    data class GetOrderItemsByOrderId(
        val orderId: String
    ) : OrderQuery() {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
    
    /**
     * 주문 ID로 주문 할인 목록 조회 쿼리
     */
    data class GetOrderDiscountsByOrderId(
        val orderId: String
    ) : OrderQuery() {
        init {
            if (orderId.isBlank()) throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
    }
    
    /**
     * 최근 N일간의 상품별 주문 수량을 집계하는 쿼리
     */
    data class GetAggregatedOrderItems(
        val days: Int = 3,
        val limit: Int = 10
    ) : OrderQuery() {
        init {
            require(days > 0) { "days must be positive" }
            require(limit > 0) { "limit must be positive" }
        }
    }
} 