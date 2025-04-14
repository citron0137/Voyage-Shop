package kr.hhplus.be.server.domain.couponevent

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CouponEventCommandTest {
    @Test
    @DisplayName("CreateCouponEventCommand가 CouponEvent 엔티티로, 변환된다")
    fun `CreateCouponEventCommand가 CouponEvent 엔티티로 변환된다`() {
        // given
        val command = CreateCouponEventCommand(
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100
        )

        // when
        val entity = command.toEntity()

        // then
        assertEquals(command.benefitMethod, entity.benefitMethod)
        assertEquals(command.benefitAmount, entity.benefitAmount)
        assertEquals(command.totalIssueAmount, entity.totalIssueAmount)
        assertEquals(command.totalIssueAmount, entity.leftIssueAmount)
    }
} 