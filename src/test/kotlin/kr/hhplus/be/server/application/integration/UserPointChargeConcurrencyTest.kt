package kr.hhplus.be.server.integration.userpoint

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.user.UserApplication
import kr.hhplus.be.server.application.userpoint.UserPointCriteria
import kr.hhplus.be.server.application.userpoint.UserPointApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@DisplayName("사용자 포인트 충전 동시성 테스트")
class UserPointChargeConcurrencyTest {

    @Autowired
    private lateinit var userApplication: UserApplication

    @Autowired
    private lateinit var userPointApplication: UserPointApplication

    @Test
    @DisplayName("여러 스레드에서 동시에 포인트를 충전해도 정확한 합계가 유지된다")
    fun `여러 스레드에서 동시에 포인트를 충전해도 정확한 합계가 유지된다`() {
        // given: 사용자 생성
        val user = userApplication.createUser()
        
        // given: 동시 충전 설정
        val threadCount = 10
        val chargeAmount = 100L
        val expectedTotalAmount = threadCount * chargeAmount
        
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        // when: 여러 스레드에서 동시에 충전 수행
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    val chargeCriteria = UserPointCriteria.Charge(user.userId, chargeAmount)
                    userPointApplication.chargePoint(chargeCriteria)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 마칠 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        // then: 최종 포인트 잔액 확인
        val getCriteria = UserPointCriteria.GetByUserId(user.userId)
        val userPoint = userPointApplication.getUserPoint(getCriteria)
        
        // 모든 충전이 정확히 반영되었는지 검증
        assertThat(userPoint.amount).isEqualTo(expectedTotalAmount)
    }
    
    @Test
    @DisplayName("더 많은 스레드에서 소액 충전을 동시에 수행해도 정확한 합계가 유지된다")
    fun `더 많은 스레드에서 소액 충전을 동시에 수행해도 정확한 합계가 유지된다`() {
        // given: 사용자 생성
        val user = userApplication.createUser()
        
        // given: 더 많은 스레드로 동시 충전 설정
        val threadCount = 50
        val chargeAmount = 10L
        val expectedTotalAmount = threadCount * chargeAmount
        
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        // when: 여러 스레드에서 동시에 충전 수행
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    val chargeCriteria = UserPointCriteria.Charge(user.userId, chargeAmount)
                    userPointApplication.chargePoint(chargeCriteria)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 마칠 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        // then: 최종 포인트 잔액 확인
        val getCriteria = UserPointCriteria.GetByUserId(user.userId)
        val userPoint = userPointApplication.getUserPoint(getCriteria)
        
        // 모든 충전이 정확히 반영되었는지 검증
        assertThat(userPoint.amount).isEqualTo(expectedTotalAmount)
    }
    
    @Test
    @DisplayName("여러 금액을 동시에 충전해도 정확한 합계가 유지된다")
    fun `여러 금액을 동시에 충전해도 정확한 합계가 유지된다`() {
        // given: 사용자 생성
        val user = userApplication.createUser()
        
        // given: 여러 금액으로 동시 충전 설정
        val threadCount = 10
        val chargeAmounts = listOf(50L, 100L, 150L, 200L, 250L, 300L, 350L, 400L, 450L, 500L)
        val expectedTotalAmount = chargeAmounts.sum()
        
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        // when: 여러 스레드에서 동시에 다른 금액으로 충전 수행
        for (i in 0 until threadCount) {
            val amount = chargeAmounts[i]
            executorService.submit {
                try {
                    val chargeCriteria = UserPointCriteria.Charge(user.userId, amount)
                    userPointApplication.chargePoint(chargeCriteria)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 마칠 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        // then: 최종 포인트 잔액 확인
        val getCriteria = UserPointCriteria.GetByUserId(user.userId)
        val userPoint = userPointApplication.getUserPoint(getCriteria)
        
        // 모든 충전이 정확히 반영되었는지 검증
        assertThat(userPoint.amount).isEqualTo(expectedTotalAmount)
    }
} 