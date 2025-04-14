package kr.hhplus.be.server.domain.couponevent

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponEventTest {
    private val now = LocalDateTime.now()

    @Test
    @DisplayName("쿠폰 이벤트의 남은 발급 수량을 감소시킬 수 있다")
    fun `쿠폰 이벤트의 남은 발급 수량을 감소시킬 수 있다`() {
        // given
        val couponEvent = CouponEvent(
            id = "coupon-event-id",
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 10,
            createdAt = now,
            updatedAt = now
        )

        // when
        couponEvent.decreaseLeftIssueAmount()

        // then
        assertEquals(9, couponEvent.leftIssueAmount)
    }

    @Test
    @DisplayName("쿠폰 이벤트의 남은 발급 수량이 0이면 감소시킬 수 없다")
    fun `쿠폰 이벤트의 남은 발급 수량이 0이면 감소시킬 수 없다`() {
        // given
        val couponEvent = CouponEvent(
            id = "coupon-event-id",
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        assertThrows<CEOutOfStockException> {
            couponEvent.decreaseLeftIssueAmount()
        }
    }

    @Test
    @DisplayName("쿠폰 발급이 가능한지 확인할 수 있다")
    fun `쿠폰 발급이 가능한지 확인할 수 있다`() {
        // given
        val couponEvent = CouponEvent(
            id = "coupon-event-id",
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 10,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        assertEquals(true, couponEvent.canIssue())
    }

    @Test
    @DisplayName("쿠폰 발급이 불가능한지 확인할 수 있다")
    fun `쿠폰 발급이 불가능한지 확인할 수 있다`() {
        // given
        val couponEvent = CouponEvent(
            id = "coupon-event-id",
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        assertEquals(false, couponEvent.canIssue())
    }

    @Test
    @DisplayName("validateCanIssue 메서드는 재고가 있을 때 예외를 발생시키지 않는다")
    fun `validateCanIssue 메서드는 재고가 있을 때 예외를 발생시키지 않는다`() {
        // given
        val couponEvent = CouponEvent(
            id = "coupon-event-id",
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 10,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        couponEvent.validateCanIssue() // 예외가 발생하지 않아야 함
    }

    @Test
    @DisplayName("validateCanIssue 메서드는 재고가 없을 때 예외를 발생시킨다")
    fun `validateCanIssue 메서드는 재고가 없을 때 예외를 발생시킨다`() {
        // given
        val couponEvent = CouponEvent(
            id = "coupon-event-id",
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        assertThrows<CEOutOfStockException> {
            couponEvent.validateCanIssue()
        }
    }
} 