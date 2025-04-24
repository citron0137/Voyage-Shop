package kr.hhplus.be.server.integration.concurrency

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.couponuser.CouponUserCriteria
import kr.hhplus.be.server.application.couponuser.CouponUserFacade
import kr.hhplus.be.server.application.couponuser.CouponUserResult
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import kr.hhplus.be.server.domain.couponuser.CouponUserException
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
import org.springframework.transaction.annotation.Transactional
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
        // 테스트 사용자 ID
        val userId = "test-user-1"
        
        // 쿠폰 발급
        val createCriteria = CouponUserCriteria.Create(
            userId = userId,
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000"
        )
        val couponResult = couponUserFacade.issueCoupon(createCriteria)
        val couponUserId = couponResult.couponUserId
        
        // 동시에 처리할 스레드 수
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        // 성공한 요청 수를 세기 위한 카운터
        val successCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        
        // 동시에 여러 스레드에서 쿠폰 사용 시도
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val useCriteria = CouponUserCriteria.Use(couponUserId)
                    val usedCouponResult = couponUserFacade.useCoupon(useCriteria)
                    
                    // 사용된 쿠폰이면 성공 카운트 증가
                    if (usedCouponResult.usedAt != null) {
                        successCount.incrementAndGet()
                    }
                    
                    logger.info("Thread $i completed successfully")
                } catch (e: CouponUserException.AlreadyUsed) {
                    // 이미 사용된 쿠폰 예외 발생 시 카운트
                    exceptionCount.incrementAndGet()
                    logger.info("Thread $i encountered AlreadyUsed exception: ${e.message}")
                } catch (e: OptimisticLockingFailureException) {
                    // 낙관적 락 실패 예외 발생 시 카운트
                    exceptionCount.incrementAndGet()
                    logger.info("Thread $i encountered OptimisticLockingFailureException: ${e.message}")
                } catch (e: ObjectOptimisticLockingFailureException) {
                    // 낙관적 락 실패 예외 발생 시 카운트
                    exceptionCount.incrementAndGet()
                    logger.info("Thread $i encountered ObjectOptimisticLockingFailureException: ${e.message}")
                } catch (e: Exception) {
                    // 기타 예외 발생 시 카운트
                    exceptionCount.incrementAndGet()
                    logger.error("Thread $i encountered unexpected exception", e)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 완료될 때까지 대기
        latch.await()
        executor.shutdown()
        
        // 결과 확인: 성공은 정확히 1번이어야 함
        assertEquals(1, successCount.get(), "쿠폰은 한 번만 사용되어야 합니다")
        assertEquals(threadCount - 1, exceptionCount.get(), "나머지는 예외가 발생해야 합니다")
        
        // 최종 쿠폰 상태 확인
        val getCriteria = CouponUserCriteria.GetById(couponUserId)
        val finalCouponState = couponUserFacade.getCouponUser(getCriteria)
        
        assertNotNull(finalCouponState.usedAt, "최종 쿠폰 상태는 사용됨 상태여야 합니다")
    }
} 