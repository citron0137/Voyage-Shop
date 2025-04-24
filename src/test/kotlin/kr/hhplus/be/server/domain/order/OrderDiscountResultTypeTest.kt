package kr.hhplus.be.server.domain.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OrderDiscountResultTypeTest {

    @Test
    @DisplayName("문자열을 DiscountType으로 변환할 수 있다")
    fun `문자열을 DiscountType으로 변환할 수 있다`() {
        // when
        val result = OrderDiscountType.fromString("COUPON")
        
        // then
        assertEquals(OrderDiscountType.COUPON, result)
    }
    
    @Test
    @DisplayName("유효하지 않은 문자열을 변환하면 예외가 발생한다")
    fun `유효하지 않은 문자열을 변환하면 예외가 발생한다`() {
        // when & then
        assertThrows(OrderException.InvalidDiscountType::class.java) {
            OrderDiscountType.fromString("INVALID_TYPE")
        }
    }
    
    @Test
    @DisplayName("DiscountType의 description을 조회할 수 있다")
    fun `DiscountType의 description을 조회할 수 있다`() {
        // when
        val result = OrderDiscountType.COUPON.description
        
        // then
        assertEquals("쿠폰 할인", result)
    }
} 