package kr.hhplus.be.server.integration.concurrency

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.couponuser.CouponUserFacade
import kr.hhplus.be.server.application.couponuser.CouponUserResult
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class CouponUserFacadeConcurrencyTest {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var couponUserFacade: CouponUserFacade

    @Autowired
    private lateinit var userFacade: UserFacade

    @Test
    @DisplayName("동시에 쿠폰 사용을 요청해도 쿠폰은 한 번만 사용 처리된다")
    fun useCouponConcurrencyTest() {
        // given: 사용자 생성 및 쿠폰 발급
        val user = userFacade.createUser()
        val userId = user.userId
        val issuedCoupon: CouponUserResult.User = couponUserFacade.issueCoupon(
            userId = userId,
            benefitMethod = CouponBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000"
        )
        val couponUserId = issuedCoupon.couponUserId

        val numberOfThreads = 50
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val optimisticLockFailCount = AtomicInteger(0)
        val alreadyUsedCount = AtomicInteger(0)

        // when: 여러 스레드에서 동시에 발급된 쿠폰 사용 시도
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    couponUserFacade.useCoupon(couponUserId)
                    successCount.incrementAndGet()
                } catch (e: ObjectOptimisticLockingFailureException) {
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: OptimisticLockingFailureException) {
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: CouponException.AlreadyUsed) {
                    alreadyUsedCount.incrementAndGet()
                } catch (e: Exception) {
                    logger.error("쿠폰 사용 중 예상치 못한 예외 발생: couponUserId={}", couponUserId, e)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await() // 모든 스레드가 완료될 때까지 대기
        executor.shutdown()

        // then: 최종 쿠폰 상태 및 성공 횟수 확인
        val finalCoupon: CouponUserResult.User = couponUserFacade.getCouponUser(couponUserId)

        logger.info("Total Attempts: $numberOfThreads")
        logger.info("Successful Uses: ${successCount.get()}")
        logger.info("Optimistic Lock Failures: ${optimisticLockFailCount.get()}")
        logger.info("Already Used Errors: ${alreadyUsedCount.get()}")
        logger.info("Final Coupon Used At: ${finalCoupon.usedAt}")

        assertEquals(1, successCount.get(), "쿠폰 사용은 정확히 한 번만 성공해야 합니다.")
        assertNotNull(finalCoupon.usedAt, "최종 쿠폰 상태는 'USED'(usedAt != null)여야 합니다.")
        assertEquals(numberOfThreads - 1, optimisticLockFailCount.get() + alreadyUsedCount.get(),
            "실패한 요청(락 실패 + 이미 사용됨)의 합은 전체 시도 횟수 - 1 이어야 합니다.")
    }
} 