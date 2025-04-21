package kr.hhplus.be.server.domain.order

enum class OrderDiscountType(val description: String) {
    COUPON("쿠폰 할인");
    
    companion object {
        fun fromString(value: String): OrderDiscountType {
            return values().firstOrNull { it.name == value }
                ?: throw OrderException.InvalidDiscountType("유효하지 않은 할인 유형입니다: $value")
        }
    }
} 