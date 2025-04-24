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
        val command = CouponEventCommand.Create(
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100
        )
        val expectedCouponEvent = CouponEvent(
            id = UUID.randomUUID().toString(),
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 100,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.create(any())).thenReturn(expectedCouponEvent)

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
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 100,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.findById(id)).thenReturn(expectedCouponEvent)

        // when
        val result = couponEventService.getCouponEvent(CouponEventQuery.GetById(id))

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
        `when`(couponEventRepository.findById(id)).thenReturn(null)

        // when & then
        assertThrows<CouponEventException.NotFound> {
            couponEventService.getCouponEvent(CouponEventQuery.GetById(id))
        }
    }

    @Test
    @DisplayName("모든 쿠폰 이벤트를 조회할 수 있다")
    fun `모든 쿠폰 이벤트를 조회할 수 있다`() {
        // given
        val couponEvents = listOf(
            CouponEvent(
                id = UUID.randomUUID().toString(),
                benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
                benefitAmount = "1000",
                totalIssueAmount = 100,
                leftIssueAmount = 100,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CouponEvent(
                id = UUID.randomUUID().toString(),
                benefitMethod = CouponEventBenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                totalIssueAmount = 50,
                leftIssueAmount = 50,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        `when`(couponEventRepository.findAll()).thenReturn(couponEvents)

        // when
        val result = couponEventService.getAllCouponEvents(CouponEventQuery.GetAll())

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
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val expectedUpdatedCouponEvent = CouponEvent(
            id = id,
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 9,
            createdAt = initialCouponEvent.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        `when`(couponEventRepository.findById(id)).thenReturn(initialCouponEvent)
        `when`(couponEventRepository.save(any())).thenReturn(expectedUpdatedCouponEvent)

        // when
        val result = couponEventService.decreaseStock(CouponEventCommand.Issue(id))

        // then
        assertEquals(expectedUpdatedCouponEvent.id, result.id)
        assertEquals(9, result.leftIssueAmount)
    }

    @Test
    @DisplayName("재고가 없는 쿠폰 이벤트의 재고를 감소시키면 예외가 발생한다")
    fun `재고가 없는 쿠폰 이벤트의 재고를 감소시키면 예외가 발생한다`() {
        // given
        val id = UUID.randomUUID().toString()
        val emptyStockCouponEvent = CouponEvent(
            id = id,
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(couponEventRepository.findById(id)).thenReturn(emptyStockCouponEvent)

        // when & then
        assertThrows<CouponEventException.OutOfStock> {
            couponEventService.decreaseStock(CouponEventCommand.Issue(id))
        }
    }

    @Test
    @DisplayName("재고가 있는 쿠폰 이벤트는 검증을 통과한다")
    fun `재고가 있는 쿠폰 이벤트는 검증을 통과한다`() {
        // given
        val couponEvent = CouponEvent(
            id = UUID.randomUUID().toString(),
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // when & then
        // 예외가 발생하지 않으면 테스트 통과
        couponEvent.validateCanIssue()
    }

    @Test
    @DisplayName("재고가 없는 쿠폰 이벤트는 검증 시 예외가 발생한다")
    fun `재고가 없는 쿠폰 이벤트는 검증 시 예외가 발생한다`() {
        // given
        val couponEvent = CouponEvent(
            id = UUID.randomUUID().toString(),
            benefitMethod = CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // when & then
        assertThrows<CouponEventException.OutOfStock> {
            couponEvent.validateCanIssue()
        }
    }
} 