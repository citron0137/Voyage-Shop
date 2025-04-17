package kr.hhplus.be.server.integration.concurrency

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.couponevent.CouponEventCriteria
import kr.hhplus.be.server.application.couponevent.CouponEventFacade
import kr.hhplus.be.server.application.couponevent.CouponEventResult
import kr.hhplus.be.server.application.couponuser.CouponUserFacade
import kr.hhplus.be.server.application.couponuser.CouponUserResult
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.domain.coupon.CouponException
import kr.hhplus.be.server.domain.couponevent.CEOutOfStockException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class CouponEventFacadeConcurrencyTest {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var couponEventFacade: CouponEventFacade

    @Autowired
    private lateinit var couponUserFacade: CouponUserFacade

    @Autowired
    private lateinit var userFacade: UserFacade

    @Test
    @DisplayName("동시에 쿠폰 발급을 요청해도 이벤트의 총 발급 수량을 초과하지 않는다")
    fun issueCouponUserConcurrencyTest() {
        // given: 발급 수량 10개인 쿠폰 이벤트 생성
        val totalIssueAmount = 10L
        val couponEvent: CouponEventResult.Get = couponEventFacade.createCouponEvent(
            CouponEventCriteria.Create(
                benefitMethod = "DISCOUNT_FIXED_AMOUNT",
                benefitAmount = "1000",
                totalIssueAmount = totalIssueAmount
            )
        )
        val couponEventId = couponEvent.id

        val numberOfThreads = 100 // 발급 수량보다 많은 사용자 시뮬레이션
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val optimisticLockFailCount = AtomicInteger(0)
        val alreadyIssuedCount = AtomicInteger(0)
        val soldOutCount = AtomicInteger(0)

        // 여러 사용자 생성
        val users = (1..numberOfThreads).map { userFacade.createUser() }

        // when: 여러 스레드에서 동시에 쿠폰 발급 시도
        users.forEach { user ->
            executor.submit {
                try {
                    couponEventFacade.issueCouponUser(
                        CouponEventCriteria.IssueCoupon(
                            userId = user.userId,
                            couponEventId = couponEventId
                        )
                    )
                    successCount.incrementAndGet()
                } catch (e: ObjectOptimisticLockingFailureException) {
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: OptimisticLockingFailureException) {
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: CouponException.AlreadyUsed) {
                    alreadyIssuedCount.incrementAndGet()
                } catch (e: CEOutOfStockException) {
                    soldOutCount.incrementAndGet()
                } catch (e: Exception) {
                    logger.error("쿠폰 발급 중 예상치 못한 예외 발생: userId={}, eventId={}", user.userId, couponEventId, e)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await() // 모든 스레드가 완료될 때까지 대기
        executor.shutdown()

        // then: 최종 발급된 쿠폰 수량 및 이벤트 상태 확인
        val finalCouponEvent: CouponEventResult.Get = couponEventFacade.getCouponEvent(CouponEventCriteria.GetById(couponEventId))
        val issuedCouponUsers: CouponUserResult.List = couponUserFacade.getAllCoupons()

        // CouponUserResult.List 내부에 couponUsers 필드 사용 (coupons -> couponUsers)
        val issuedCountForThisEvent = issuedCouponUsers.couponUsers.size

        logger.info("Total Attempts: $numberOfThreads")
        logger.info("Successful Issues: ${successCount.get()}")
        logger.info("Optimistic Lock Failures: ${optimisticLockFailCount.get()}")
        logger.info("Already Issued Errors: ${alreadyIssuedCount.get()}")
        logger.info("Sold Out Errors: ${soldOutCount.get()}")
        logger.info("Final Event Stock (Remaining): ${finalCouponEvent.leftIssueAmount}")
        // CouponUserResult.List 내부에 couponUsers 필드 사용 (coupons -> couponUsers)
        logger.info("Total Issued Coupons in System (Approx.): ${issuedCouponUsers.couponUsers.size}")

        assertEquals(totalIssueAmount, successCount.get().toLong(), "성공한 쿠폰 발급 수는 이벤트의 총 발급 수량과 같아야 합니다.")
        assertEquals(0L, finalCouponEvent.leftIssueAmount, "이벤트의 현재 남은 발급 가능 수량(leftIssueAmount)은 0이어야 합니다.")
    }
} 