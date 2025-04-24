package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.couponuser.CouponUser
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import kr.hhplus.be.server.domain.couponuser.CouponUserException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponUserTest {
    private val now = LocalDateTime.now()

    @Test
    @DisplayName("쿠폰을 사용하면 usedAt이 현재 시간으로 설정된다")
    fun `쿠폰을 사용하면 usedAt이 현재 시간으로 설정된다`() {
        // given
        val couponUser = CouponUser(
            couponUserId = "coupon-user-id",
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )

        // when
        val usedCouponUser = couponUser.use()

        // then
        assertEquals(couponUser.couponUserId, usedCouponUser.couponUserId)
        assertEquals(couponUser.userId, usedCouponUser.userId)
        assertEquals(couponUser.benefitMethod, usedCouponUser.benefitMethod)
        assertEquals(couponUser.benefitAmount, usedCouponUser.benefitAmount)
        assertEquals(now, usedCouponUser.createdAt)
        assertEquals(now, usedCouponUser.updatedAt)
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 사용하면 예외가 발생한다")
    fun `이미 사용된 쿠폰을 다시 사용하면 예외가 발생한다`() {
        // given
        val couponUser = CouponUser(
            couponUserId = "coupon-user-id",
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = now,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        assertThrows<CouponUserException.AlreadyUsed> {
            couponUser.use()
        }
    }

    @Test
    @DisplayName("고정 금액 할인 쿠폰의 할인 금액을 계산한다")
    fun `고정 금액 할인 쿠폰의 할인 금액을 계산한다`() {
        // given
        val couponUser = CouponUser(
            couponUserId = "coupon-user-id",
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )

        // when
        val discountAmount = couponUser.calculateDiscountAmount(5000)

        // then
        assertEquals(1000, discountAmount)
    }

    @Test
    @DisplayName("퍼센트 할인 쿠폰의 할인 금액을 계산한다")
    fun `퍼센트 할인 쿠폰의 할인 금액을 계산한다`() {
        // given
        val couponUser = CouponUser(
            couponUserId = "coupon-user-id",
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
            benefitAmount = "10",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )

        // when
        val discountAmount = couponUser.calculateDiscountAmount(5000)

        // then
        assertEquals(500, discountAmount)
    }

    @Test
    @DisplayName("고정 금액 할인 쿠폰의 할인 금액이 원래 금액보다 크면 예외가 발생한다")
    fun `고정 금액 할인 쿠폰의 할인 금액이 원래 금액보다 크면 예외가 발생한다`() {
        // given
        val couponUser = CouponUser(
            couponUserId = "coupon-user-id",
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "2000",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        assertThrows<CouponUserException.DiscountAmountExceedsOriginalAmount> {
            couponUser.calculateDiscountAmount(1000)
        }
    }

    @Test
    @DisplayName("퍼센트 할인 쿠폰의 할인율이 100%를 초과하면 예외가 발생한다")
    fun `퍼센트 할인 쿠폰의 할인율이 100%를 초과하면 예외가 발생한다`() {
        // given
        val couponUser = CouponUser(
            couponUserId = "coupon-user-id",
            userId = "user-id",
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_PERCENTAGE,
            benefitAmount = "120",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )

        // when & then
        assertThrows<CouponUserException.DiscountPercentageExceeds100> {
            couponUser.calculateDiscountAmount(5000)
        }
    }

    @Test
    @DisplayName("잘못된 할인 방법으로 쿠폰을 생성하면 예외가 발생한다")
    fun `잘못된 할인 방법으로 쿠폰을 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<CouponUserException.InvalidBenefitMethod> {
            CouponUserBenefitMethod.from("INVALID_METHOD")
        }
    }
} 