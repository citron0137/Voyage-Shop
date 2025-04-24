package kr.hhplus.be.server.integration.concurrency

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.application.userpoint.UserPointCriteria
import kr.hhplus.be.server.application.userpoint.UserPointFacade
import kr.hhplus.be.server.application.userpoint.UserPointResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
class UserPointFacadeConcurrencyTest {

    @Autowired
    private lateinit var userPointFacade: UserPointFacade

    @Autowired
    private lateinit var userFacade: UserFacade

    @Test
    @DisplayName("동시에 사용자 포인트를 충전해도 정확한 잔액이 유지된다")
    fun chargePointConcurrencyTest() {
        // given: 사용자 생성 및 초기 포인트 확인 (0이어야 함)
        val user = userFacade.createUser()
        val userId = user.userId
        val initialPoint: UserPointResult.Point = userPointFacade.getUserPoint(UserPointCriteria.GetByUserId(userId))
        assertEquals(0L, initialPoint.amount, "초기 포인트는 0이어야 합니다.")

        val numberOfThreads = 100
        val chargeAmount = 10L
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val optimisticLockFailCount = AtomicInteger(0)

        // when: 여러 스레드에서 동시에 포인트 10 충전 시도
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    userPointFacade.chargePoint(
                        UserPointCriteria.Charge(
                            userId = userId,
                            amount = chargeAmount
                        )
                    )
                    successCount.incrementAndGet()
                } catch (e: ObjectOptimisticLockingFailureException) {
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: OptimisticLockingFailureException) {
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: Exception) {
                    println("예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await() // 모든 스레드가 완료될 때까지 대기
        executor.shutdown()

        // then: 최종 포인트 잔액 확인
        val finalPoint: UserPointResult.Point = userPointFacade.getUserPoint(UserPointCriteria.GetByUserId(userId))
        val expectedBalance = chargeAmount * successCount.get() // 성공한 만큼만 충전

        println("Total Attempts: $numberOfThreads")
        println("Successful Charges: ${successCount.get()}")
        println("Optimistic Lock Failures: ${optimisticLockFailCount.get()}")
        println("Expected Final Balance: $expectedBalance")
        println("Actual Final Balance: ${finalPoint.amount}")

        assertEquals(expectedBalance, finalPoint.amount, "최종 포인트는 성공한 충전 횟수 * 충전 금액이어야 합니다.")
    }
} 