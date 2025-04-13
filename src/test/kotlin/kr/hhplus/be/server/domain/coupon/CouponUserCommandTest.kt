package kr.hhplus.be.server.domain.coupon

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CouponUserCommandTest {
    @Test
    @DisplayName("Create 커맨드가 엔티티로 변환된다")
    fun `Create 커맨드가 엔티티로 변환된다`() {
        // given
        val command = CouponUserCommand.Create(
            userId = "user-id",
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000"
        )

        // when
        val entity = command.toEntity()

        // then
        assertEquals(command.userId, entity.userId)
        assertEquals(CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT, entity.benefitMethod)
        assertEquals(command.benefitAmount, entity.benefitAmount)
        assertEquals(null, entity.usedAt)
    }

    @Test
    @DisplayName("빈 userId로 Create 커맨드를 생성하면 예외가 발생한다")
    fun `빈 userId로 Create 커맨드를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<CouponException.UserIdShouldNotBlank> {
            CouponUserCommand.Create(
                userId = "",
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000"
            )
        }
    }

    @Test
    @DisplayName("빈 benefitAmount로 Create 커맨드를 생성하면 예외가 발생한다")
    fun `빈 benefitAmount로 Create 커맨드를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<CouponException.BenefitAmountShouldNotBlank> {
            CouponUserCommand.Create(
                userId = "user-id",
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = ""
            )
        }
    }

    @Test
    @DisplayName("0 이하의 할인 금액으로 Create 커맨드를 생성하면 예외가 발생한다")
    fun `0 이하의 할인 금액으로 Create 커맨드를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<CouponException.BenefitAmountShouldMoreThan0> {
            CouponUserCommand.Create(
                userId = "user-id",
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "0"
            )
        }
    }

    @Test
    @DisplayName("숫자가 아닌 할인 금액으로 Create 커맨드를 생성하면 예외가 발생한다")
    fun `숫자가 아닌 할인 금액으로 Create 커맨드를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<CouponException.BenefitAmountShouldBeNumeric> {
            CouponUserCommand.Create(
                userId = "user-id",
                benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "invalid"
            )
        }
    }

    @Test
    @DisplayName("퍼센트 할인율이 100%를 초과하는 Create 커맨드를 생성하면 예외가 발생한다")
    fun `퍼센트 할인율이 100%를 초과하는 Create 커맨드를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<CouponException.DiscountPercentageExceeds100> {
            CouponUserCommand.Create(
                userId = "user-id",
                benefitMethod = CouponBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "120"
            )
        }
    }

    @Test
    @DisplayName("빈 couponUserId로 Use 커맨드를 생성하면 예외가 발생한다")
    fun `빈 couponUserId로 Use 커맨드를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<CouponException.CouponUserIdShouldNotBlank> {
            CouponUserCommand.Use(
                couponUserId = ""
            )
        }
    }
} 