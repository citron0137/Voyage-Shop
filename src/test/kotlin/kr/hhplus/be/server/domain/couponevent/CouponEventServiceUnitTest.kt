package kr.hhplus.be.server.domain.couponevent

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CouponEventServiceUnitTest {
    @Mock
    private lateinit var couponEventRepository: CouponEventRepository

    @InjectMocks
    private lateinit var couponEventService: CouponEventService

    @Test
    @DisplayName("쿠폰 이벤트를 생성할 수 있다")
    fun `쿠폰 이벤트를 생성할 수 있다`() {
        // given
        val command = CreateCouponEventCommand(
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100
        )
        val expectedCouponEvent = CouponEvent(
            id = UUID.randomUUID().toString(),
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 100,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.save(any<CouponEvent>())).thenReturn(expectedCouponEvent)

        // when
        val result = couponEventService.createCouponEvent(command)

        // then
        assertEquals(expectedCouponEvent.id, result.id)
        assertEquals(command.benefitMethod, result.benefitMethod)
        assertEquals(command.benefitAmount, result.benefitAmount)
        assertEquals(command.totalIssueAmount, result.totalIssueAmount)
        assertEquals(command.totalIssueAmount, result.leftIssueAmount)
    }

    @Test
    @DisplayName("ID로 쿠폰 이벤트를 조회할 수 있다")
    fun `ID로 쿠폰 이벤트를 조회할 수 있다`() {
        // given
        val id = UUID.randomUUID().toString()
        val expectedCouponEvent = CouponEvent(
            id = id,
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 100,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.findById(id)).thenReturn(Optional.of(expectedCouponEvent))

        // when
        val result = couponEventService.getCouponEvent(id)

        // then
        assertEquals(expectedCouponEvent.id, result.id)
        assertEquals(expectedCouponEvent.benefitMethod, result.benefitMethod)
        assertEquals(expectedCouponEvent.benefitAmount, result.benefitAmount)
        assertEquals(expectedCouponEvent.totalIssueAmount, result.totalIssueAmount)
        assertEquals(expectedCouponEvent.leftIssueAmount, result.leftIssueAmount)
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
    fun `존재하지 않는 ID로 조회하면 예외가 발생한다`() {
        // given
        val id = UUID.randomUUID().toString()
        `when`(couponEventRepository.findById(id)).thenReturn(Optional.empty())

        // when & then
        assertThrows<CENotFoundException> {
            couponEventService.getCouponEvent(id)
        }
    }

    @Test
    @DisplayName("모든 쿠폰 이벤트를 조회할 수 있다")
    fun `모든 쿠폰 이벤트를 조회할 수 있다`() {
        // given
        val couponEvents = listOf(
            CouponEvent(
                id = UUID.randomUUID().toString(),
                benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                totalIssueAmount = 100,
                leftIssueAmount = 100,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponEvent(
                id = UUID.randomUUID().toString(),
                benefitMethod = BenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                totalIssueAmount = 50,
                leftIssueAmount = 50,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        `when`(couponEventRepository.findAll()).thenReturn(couponEvents)

        // when
        val result = couponEventService.getAllCouponEvents()

        // then
        assertEquals(2, result.size)
        assertEquals(couponEvents[0].id, result[0].id)
        assertEquals(couponEvents[1].id, result[1].id)
    }

    @Test
    @DisplayName("쿠폰 이벤트의 재고를 감소시킬 수 있다")
    fun `쿠폰 이벤트의 재고를 감소시킬 수 있다`() {
        // given
        val id = UUID.randomUUID().toString()
        val initialCouponEvent = CouponEvent(
            id = id,
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val expectedUpdatedCouponEvent = initialCouponEvent.copy(leftIssueAmount = 9)
        
        `when`(couponEventRepository.findById(id)).thenReturn(Optional.of(initialCouponEvent))
        `when`(couponEventRepository.save(any<CouponEvent>())).thenReturn(expectedUpdatedCouponEvent)

        // when
        val result = couponEventService.decreaseStock(id)

        // then
        assertEquals(expectedUpdatedCouponEvent.id, result.id)
        assertEquals(9, result.leftIssueAmount)
    }

    @Test
    @DisplayName("재고가 없는 쿠폰 이벤트의 재고를 감소시키면 예외가 발생한다")
    fun `재고가 없는 쿠폰 이벤트의 재고를 감소시키면 예외가 발생한다`() {
        // given
        val id = UUID.randomUUID().toString()
        val couponEvent = CouponEvent(
            id = id,
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.findById(id)).thenReturn(Optional.of(couponEvent))

        // when & then
        assertThrows<CEStockEmptyException> {
            couponEventService.decreaseStock(id)
        }
    }

    @Test
    @DisplayName("쿠폰 발급 가능 여부를 확인할 수 있다")
    fun `쿠폰 발급 가능 여부를 확인할 수 있다`() {
        // given
        val id = UUID.randomUUID().toString()
        val couponEvent = CouponEvent(
            id = id,
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.findById(id)).thenReturn(Optional.of(couponEvent))

        // when
        val result = couponEventService.canIssue(id)

        // then
        assertEquals(true, result)
    }

    @Test
    @DisplayName("재고가 없는 쿠폰 이벤트는 발급 불가능함을 확인할 수 있다")
    fun `재고가 없는 쿠폰 이벤트는 발급 불가능함을 확인할 수 있다`() {
        // given
        val id = UUID.randomUUID().toString()
        val couponEvent = CouponEvent(
            id = id,
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.findById(id)).thenReturn(Optional.of(couponEvent))

        // when
        val result = couponEventService.canIssue(id)

        // then
        assertEquals(false, result)
    }
} 