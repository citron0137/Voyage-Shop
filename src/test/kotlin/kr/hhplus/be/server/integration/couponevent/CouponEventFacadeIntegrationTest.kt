package kr.hhplus.be.server.integration.couponevent

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.couponevent.CouponEventCriteria
import kr.hhplus.be.server.application.couponevent.CouponEventFacade
import kr.hhplus.be.server.domain.couponevent.CouponEventBenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEventException
import kr.hhplus.be.server.domain.couponuser.CouponUserService
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserCommand
import kr.hhplus.be.server.domain.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@DisplayName("쿠폰 이벤트 통합 테스트")
class CouponEventFacadeIntegrationTest {

    @Autowired
    private lateinit var couponEventFacade: CouponEventFacade

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var couponUserService: CouponUserService
    
    private val testUsers = mutableListOf<User>()
    
    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 생성
        for (i in 1..5) {
            val user = userService.createUser(UserCommand.Create)
            testUsers.add(user)
        }
    }

    @Test
    @DisplayName("쿠폰 이벤트를 생성할 수 있다")
    @Transactional
    fun createCouponEventTest() {
        // given
        val criteria = CouponEventCriteria.Create(
            benefitMethod = "DISCOUNT_FIXED_AMOUNT",
            benefitAmount = "1000",
            totalIssueAmount = 100
        )

        // when
        val result = couponEventFacade.createCouponEvent(criteria)

        // then
        assertThat(result.id).isNotNull()
        assertThat(result.benefitMethod).isEqualTo(CouponEventBenefitMethod.DISCOUNT_FIXED_AMOUNT)
        assertThat(result.benefitAmount).isEqualTo("1000")
        assertThat(result.totalIssueAmount).isEqualTo(100)
        assertThat(result.leftIssueAmount).isEqualTo(100)
    }

    @Test
    @DisplayName("ID로 쿠폰 이벤트를 조회할 수 있다")
    @Transactional
    fun getCouponEventTest() {
        // given
        val createCriteria = CouponEventCriteria.Create(
            benefitMethod = "DISCOUNT_PERCENTAGE",
            benefitAmount = "10",
            totalIssueAmount = 50
        )
        val createdEvent = couponEventFacade.createCouponEvent(createCriteria)
        
        // when
        val getCriteria = CouponEventCriteria.GetById(createdEvent.id)
        val result = couponEventFacade.getCouponEvent(getCriteria)
        
        // then
        assertThat(result.id).isEqualTo(createdEvent.id)
        assertThat(result.benefitMethod).isEqualTo(CouponEventBenefitMethod.DISCOUNT_PERCENTAGE)
        assertThat(result.benefitAmount).isEqualTo("10")
    }

    @Test
    @DisplayName("존재하지 않는 ID로 쿠폰 이벤트를 조회하면 예외가 발생한다")
    @Transactional
    fun getCouponEventNotFoundTest() {
        // given
        val notExistingId = "not-existing-id"
        val getCriteria = CouponEventCriteria.GetById(notExistingId)
        
        // when & then
        assertThrows<CouponEventException.NotFound> {
            couponEventFacade.getCouponEvent(getCriteria)
        }
    }

    @Test
    @DisplayName("모든 쿠폰 이벤트를 조회할 수 있다")
    @Transactional
    fun getAllCouponEventsTest() {
        // given
        val initialCount = couponEventFacade.getAllCouponEvents().couponEvents.size
        
        // 쿠폰 이벤트 2개 생성
        couponEventFacade.createCouponEvent(
            CouponEventCriteria.Create(
                benefitMethod = "DISCOUNT_FIXED_AMOUNT",
                benefitAmount = "2000",
                totalIssueAmount = 30
            )
        )
        
        couponEventFacade.createCouponEvent(
            CouponEventCriteria.Create(
                benefitMethod = "DISCOUNT_PERCENTAGE",
                benefitAmount = "20",
                totalIssueAmount = 40
            )
        )
        
        // when
        val result = couponEventFacade.getAllCouponEvents()
        
        // then
        assertThat(result.couponEvents.size).isEqualTo(initialCount + 2)
    }

    @Test
    @DisplayName("쿠폰을 발급받을 수 있다")
    @Transactional
    fun issueCouponUserTest() {
        // given
        val createCriteria = CouponEventCriteria.Create(
            benefitMethod = "DISCOUNT_FIXED_AMOUNT",
            benefitAmount = "3000",
            totalIssueAmount = 5
        )
        val createdEvent = couponEventFacade.createCouponEvent(createCriteria)
        val userId = testUsers[0].userId
        
        // when
        val issueCriteria = CouponEventCriteria.IssueCoupon(
            couponEventId = createdEvent.id,
            userId = userId
        )
        val result = couponEventFacade.issueCouponUser(issueCriteria)
        
        // then
        // IssueCoupon 클래스는 couponUserId 필드만 있음
        assertThat(result.couponUserId).isNotNull()
        
        // 쿠폰 발급 후 이벤트의 남은 발급 수량 확인
        val updatedEvent = couponEventFacade.getCouponEvent(CouponEventCriteria.GetById(createdEvent.id))
        assertThat(updatedEvent.leftIssueAmount).isEqualTo(4) // 처음 5개에서 1개 발급했으므로
    }

    @Test
    @DisplayName("재고가 없으면 쿠폰을 발급받을 수 없다")
    @Transactional
    fun issueCouponUserOutOfStockTest() {
        // given
        val createCriteria = CouponEventCriteria.Create(
            benefitMethod = "DISCOUNT_PERCENTAGE",
            benefitAmount = "15",
            totalIssueAmount = 1 // 재고를 1개만 설정
        )
        val createdEvent = couponEventFacade.createCouponEvent(createCriteria)
        
        // 먼저 재고 1개 소진
        val issueCriteria1 = CouponEventCriteria.IssueCoupon(
            couponEventId = createdEvent.id,
            userId = testUsers[1].userId
        )
        couponEventFacade.issueCouponUser(issueCriteria1)
        
        // when & then - 두 번째 발급 시도에서 예외 발생
        val issueCriteria2 = CouponEventCriteria.IssueCoupon(
            couponEventId = createdEvent.id,
            userId = testUsers[2].userId
        )
        assertThrows<CouponEventException.OutOfStock> {
            couponEventFacade.issueCouponUser(issueCriteria2)
        }
    }
} 