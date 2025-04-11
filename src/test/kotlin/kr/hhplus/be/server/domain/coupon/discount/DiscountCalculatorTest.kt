package kr.hhplus.be.server.domain.coupon.discount

import kr.hhplus.be.server.domain.coupon.CouponException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DiscountCalculatorTest {
    private val fixedAmountCalculator = FixedAmountDiscountCalculator()
    private val percentageCalculator = PercentageDiscountCalculator()
    
    @Test
    @DisplayName("고정 금액 할인 계산기는 할인 금액을 그대로 반환한다")
    fun `고정 금액 할인 계산기는 할인 금액을 그대로 반환한다`() {
        // given
        val originalAmount = 5000L
        val discountAmount = "1000"
        
        // when
        val result = fixedAmountCalculator.calculate(originalAmount, discountAmount)
        
        // then
        assertEquals(1000L, result)
    }
    
    @Test
    @DisplayName("고정 금액 할인 계산기는 할인 금액이 원래 금액보다 크면 예외를 발생시킨다")
    fun `고정 금액 할인 계산기는 할인 금액이 원래 금액보다 크면 예외를 발생시킨다`() {
        // given
        val originalAmount = 1000L
        val discountAmount = "2000"
        
        // when & then
        assertThrows<CouponException.DiscountAmountExceedsOriginalAmount> {
            fixedAmountCalculator.calculate(originalAmount, discountAmount)
        }
    }
    
    @Test
    @DisplayName("퍼센트 할인 계산기는 원래 금액에 할인율을 적용한 금액을 반환한다")
    fun `퍼센트 할인 계산기는 원래 금액에 할인율을 적용한 금액을 반환한다`() {
        // given
        val originalAmount = 5000L
        val discountAmount = "10"
        
        // when
        val result = percentageCalculator.calculate(originalAmount, discountAmount)
        
        // then
        assertEquals(500L, result)
    }
    
    @Test
    @DisplayName("퍼센트 할인 계산기는 할인율이 100%를 초과하면 예외를 발생시킨다")
    fun `퍼센트 할인 계산기는 할인율이 100%를 초과하면 예외를 발생시킨다`() {
        // given
        val originalAmount = 5000L
        val discountAmount = "120"
        
        // when & then
        assertThrows<CouponException.DiscountPercentageExceeds100> {
            percentageCalculator.calculate(originalAmount, discountAmount)
        }
    }
    
    @Test
    @DisplayName("할인 계산기 팩토리는 할인 방식에 맞는 계산기를 반환한다")
    fun `할인 계산기 팩토리는 할인 방식에 맞는 계산기를 반환한다`() {
        // given
        val factory = DiscountCalculatorFactory
        
        // when & then
        val fixedCalculator = factory.getCalculator(kr.hhplus.be.server.domain.coupon.CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT)
        val percentageCalculator = factory.getCalculator(kr.hhplus.be.server.domain.coupon.CouponBenefitMethod.DISCOUNT_PERCENTAGE)
        
        // then
        assertEquals(FixedAmountDiscountCalculator::class.java, fixedCalculator.javaClass)
        assertEquals(PercentageDiscountCalculator::class.java, percentageCalculator.javaClass)
    }
} 