package kr.hhplus.be.server.integration.concurrency

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.application.order.OrderApplication
import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductApplication
import kr.hhplus.be.server.application.user.UserApplication
import kr.hhplus.be.server.domain.product.ProductException
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
class OrderApplicationConcurrencyTest {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var orderApplication: OrderApplication

    @Autowired
    private lateinit var productApplication: ProductApplication

    @Autowired
    private lateinit var userApplication: UserApplication

    @Test
    @DisplayName("동시에 주문을 생성해도 상품 재고가 정확히 차감된다")
    fun createOrderConcurrencyTest() {
        // given: 재고 10개인 상품 생성
        val initialStock = 10L
        val product = productApplication.createProduct(
            ProductCriteria.Create(
                name = "Test Product for Order Concurrency",
                price = 1000,
                stock = initialStock
            )
        )
        val productId = product.productId

        val numberOfThreads = 50 // 재고보다 많은 주문 시도
        val orderAmount = 1L
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val optimisticLockFailCount = AtomicInteger(0)
        val stockUnderflowCount = AtomicInteger(0)

        // 여러 사용자 생성
        val users = (1..numberOfThreads).map { userApplication.createUser() }

        // when: 여러 사용자가 동시에 상품 1개 주문 시도
        users.forEach { user ->
            executor.submit {
                try {
                    orderApplication.createOrder(
                        OrderCriteria.Create(
                            userId = user.userId,
                            items = listOf(
                                OrderCriteria.Create.OrderItem(
                                    productId = productId,
                                    amount = orderAmount
                                )
                            )
                        )
                    )
                    successCount.incrementAndGet()
                } catch (e: ObjectOptimisticLockingFailureException) {
                    // 주문 과정 중 상품 재고 변경에서 발생 가능
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: OptimisticLockingFailureException) {
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: ProductException.StockAmountUnderflow) {
                    // 재고 부족 예외 카운트
                    stockUnderflowCount.incrementAndGet()
                } catch (e: Exception) {
                    logger.error("주문 생성 중 예상치 못한 예외 발생: userId={}, productId={}", user.userId, productId, e)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await() // 모든 스레드가 완료될 때까지 대기
        executor.shutdown()

        // then: 최종 상품 재고 확인
        val finalProduct = productApplication.getProduct(ProductCriteria.GetById(productId))
        val expectedStock = initialStock - successCount.get() // 성공한 주문 만큼만 감소

        logger.info("Total Attempts: $numberOfThreads")
        logger.info("Successful Orders: ${successCount.get()}")
        logger.info("Optimistic Lock Failures: ${optimisticLockFailCount.get()}")
        logger.info("Stock Underflow Errors: ${stockUnderflowCount.get()}")
        logger.info("Initial Stock: $initialStock")
        logger.info("Expected Final Stock: $expectedStock")
        logger.info("Actual Final Stock: ${finalProduct.stock}")

        assertEquals(initialStock, product.stock, "테스트 시작 시 초기 재고가 정확해야 합니다.")
        assertEquals(expectedStock, finalProduct.stock, "최종 재고는 성공한 주문량만큼 줄어들어야 합니다.")
        // 성공한 주문 수가 초기 재고를 넘지 않아야 함
        assert(successCount.get() <= initialStock) { "성공한 주문 수는 초기 재고(${initialStock})보다 많을 수 없습니다." }
    }
} 