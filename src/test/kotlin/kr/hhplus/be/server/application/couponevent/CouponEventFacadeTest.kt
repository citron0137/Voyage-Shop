package kr.hhplus.be.server.application.couponevent

import kr.hhplus.be.server.application.couponevent.dto.CreateCouponEventCriteria
import kr.hhplus.be.server.application.couponevent.dto.IssueCouponCriteria
import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUser
import kr.hhplus.be.server.domain.coupon.CouponUserCommand
import kr.hhplus.be.server.domain.coupon.CouponUserService
import kr.hhplus.be.server.domain.couponevent.BenefitMethod
import kr.hhplus.be.server.domain.couponevent.CEInvalidBenefitMethodException
import kr.hhplus.be.server.domain.couponevent.CENotFoundException
import kr.hhplus.be.server.domain.couponevent.CEStockEmptyException
import kr.hhplus.be.server.domain.couponevent.CouponEvent
import kr.hhplus.be.server.domain.couponevent.CouponEventService
import kr.hhplus.be.server.domain.couponevent.CreateCouponEventCommand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import java.util.UUID

@ExtendWith(MockitoExtension::class)
@DisplayName("CouponEventFacade 테스트")
class CouponEventFacadeTest {

    @Mock
    private lateinit var couponEventService: CouponEventService

    @Mock
    private lateinit var couponUserService: CouponUserService

    @InjectMocks
    private lateinit var couponEventFacade: CouponEventFacade

    private lateinit var sampleCouponEvent: CouponEvent
    private lateinit var now: LocalDateTime

    @BeforeEach
    fun setup() {
        now = LocalDateTime.now()
        sampleCouponEvent = CouponEvent(
            id = "event-id",
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 50,
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    @DisplayName("쿠폰 이벤트를 생성할 수 있다")
    fun `쿠폰 이벤트를 생성할 수 있다`() {
        // given
        val criteria = CreateCouponEventCriteria(
            benefitMethod = "DISCOUNT_FIXED_AMOUNT",
            benefitAmount = "1000",
            totalIssueAmount = 100
        )
        
        `when`(couponEventService.createCouponEvent(any<CreateCouponEventCommand>())).thenReturn(sampleCouponEvent)
        
        // when
        val result = couponEventFacade.createCouponEvent(criteria)
        
        // then
        assertThat(result.id).isEqualTo(sampleCouponEvent.id)
        assertThat(result.benefitMethod).isEqualTo(sampleCouponEvent.benefitMethod.name)
        assertThat(result.benefitAmount).isEqualTo(sampleCouponEvent.benefitAmount)
        assertThat(result.totalIssueAmount).isEqualTo(sampleCouponEvent.totalIssueAmount)
        assertThat(result.leftIssueAmount).isEqualTo(sampleCouponEvent.leftIssueAmount)
        
        verify(couponEventService, times(1)).createCouponEvent(any<CreateCouponEventCommand>())
    }
    
    @Test
    @DisplayName("잘못된 혜택 방식으로 쿠폰 이벤트를 생성하면 예외가 발생한다")
    fun `잘못된 혜택 방식으로 쿠폰 이벤트를 생성하면 예외가 발생한다`() {
        // given
        val criteria = CreateCouponEventCriteria(
            benefitMethod = "INVALID_METHOD",
            benefitAmount = "1000",
            totalIssueAmount = 100
        )
        
        // when & then
        assertThrows(CEInvalidBenefitMethodException::class.java) {
            couponEventFacade.createCouponEvent(criteria)
        }
    }
    
    @Test
    @DisplayName("모든 쿠폰 이벤트를 조회할 수 있다")
    fun `모든 쿠폰 이벤트를 조회할 수 있다`() {
        // given
        val couponEvents = listOf(
            sampleCouponEvent,
            CouponEvent(
                id = "event-id-2",
                benefitMethod = BenefitMethod.DISCOUNT_PERCENTAGE,
                benefitAmount = "10",
                totalIssueAmount = 50,
                leftIssueAmount = 25,
                createdAt = now,
                updatedAt = now
            )
        )
        
        `when`(couponEventService.getAllCouponEvents()).thenReturn(couponEvents)
        
        // when
        val result = couponEventFacade.getAllCouponEvents()
        
        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(couponEvents[0].id)
        assertThat(result[0].benefitMethod).isEqualTo(couponEvents[0].benefitMethod.name)
        assertThat(result[1].id).isEqualTo(couponEvents[1].id)
        assertThat(result[1].benefitMethod).isEqualTo(couponEvents[1].benefitMethod.name)
        
        verify(couponEventService, times(1)).getAllCouponEvents()
    }
    
    @Test
    @DisplayName("쿠폰 이벤트로부터 쿠폰을 발급할 수 있다")
    fun `쿠폰 이벤트로부터 쿠폰을 발급할 수 있다`() {
        // given
        val couponEventId = "event-id"
        val userId = "user-id"
        val criteria = IssueCouponCriteria(userId = userId)
        
        val couponUser = CouponUser(
            couponUserId = UUID.randomUUID().toString(),
            userId = userId,
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            usedAt = null,
            createdAt = now,
            updatedAt = now
        )
        
        `when`(couponEventService.getCouponEvent(couponEventId)).thenReturn(sampleCouponEvent)
        `when`(couponEventService.decreaseStock(couponEventId)).thenReturn(sampleCouponEvent.copy(leftIssueAmount = 49))
        `when`(couponUserService.create(any<CouponUserCommand.Create>())).thenReturn(couponUser)
        
        // when
        val result = couponEventFacade.issueCouponUser(couponEventId, criteria)
        
        // then
        assertThat(result.couponUserId).isEqualTo(couponUser.couponUserId)
        
        verify(couponEventService, times(1)).getCouponEvent(couponEventId)
        verify(couponEventService, times(1)).decreaseStock(couponEventId)
        verify(couponUserService, times(1)).create(any<CouponUserCommand.Create>())
    }
    
    @Test
    @DisplayName("존재하지 않는 쿠폰 이벤트에서 쿠폰을 발급하면 예외가 발생한다")
    fun `존재하지 않는 쿠폰 이벤트에서 쿠폰을 발급하면 예외가 발생한다`() {
        // given
        val couponEventId = "non-existing-event-id"
        val criteria = IssueCouponCriteria(userId = "user-id")
        
        `when`(couponEventService.getCouponEvent(couponEventId)).thenThrow(CENotFoundException(couponEventId))
        
        // when & then
        assertThrows(CENotFoundException::class.java) {
            couponEventFacade.issueCouponUser(couponEventId, criteria)
        }
    }
    
    @Test
    @DisplayName("재고가 없는 쿠폰 이벤트에서 쿠폰을 발급하면 예외가 발생한다")
    fun `재고가 없는 쿠폰 이벤트에서 쿠폰을 발급하면 예외가 발생한다`() {
        // given
        val couponEventId = "event-id-no-stock"
        val criteria = IssueCouponCriteria(userId = "user-id")
        
        val emptyStockEvent = CouponEvent(
            id = couponEventId,
            benefitMethod = BenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000",
            totalIssueAmount = 100,
            leftIssueAmount = 0,
            createdAt = now,
            updatedAt = now
        )
        
        `when`(couponEventService.getCouponEvent(couponEventId)).thenReturn(emptyStockEvent)
        
        // when & then
        assertThrows(CEStockEmptyException::class.java) {
            couponEventFacade.issueCouponUser(couponEventId, criteria)
        }
    }
    
    @Test
    @DisplayName("재고 감소 시 예외가 발생하면 쿠폰 발급이 실패한다")
    fun `재고 감소 시 예외가 발생하면 쿠폰 발급이 실패한다`() {
        // given
        val couponEventId = "event-id"
        val criteria = IssueCouponCriteria(userId = "user-id")
        
        `when`(couponEventService.getCouponEvent(couponEventId)).thenReturn(sampleCouponEvent)
        `when`(couponEventService.decreaseStock(couponEventId)).thenThrow(CEStockEmptyException(couponEventId))
        
        // when & then
        assertThrows(CEStockEmptyException::class.java) {
            couponEventFacade.issueCouponUser(couponEventId, criteria)
        }
    }
} 