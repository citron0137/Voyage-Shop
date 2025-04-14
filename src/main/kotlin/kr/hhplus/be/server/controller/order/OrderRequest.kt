package kr.hhplus.be.server.controller.order

/**
 * 주문 요청 관련 클래스
 */
class OrderRequest {
    /**
     * 주문 생성 요청
     */
    data class Create(
        val userId: String,
        val orderItemList: List<OrderItem>,
        val payment: Payment
    )
    
    /**
     * 주문 항목 요청
     */
    data class OrderItem(
        val productId: String,
        val count: Long
    )
    
    /**
     * 결제 정보 요청
     */
    data class Payment(
        val couponId: String?
    )
} 