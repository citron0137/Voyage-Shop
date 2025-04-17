package kr.hhplus.be.server.integration.concurrency

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.domain.product.ProductException
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
class ProductFacadeConcurrencyTest {

    @Autowired
    private lateinit var productFacade: ProductFacade

    @Test
    @DisplayName("동시에 상품 재고를 감소시켜도 정확한 재고가 유지된다")
    fun decreaseStockConcurrencyTest() {
        // given: 초기 재고 100개인 상품 생성
        val initialStock = 100L
        val createdProduct = productFacade.createProduct(
            ProductCriteria.Create(
                name = "Test Product for Concurrency",
                price = 1000,
                stock = initialStock
            )
        )
        val productId = createdProduct.productId

        val numberOfThreads = 50
        val decreaseAmount = 1L
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val optimisticLockFailCount = AtomicInteger(0)
        val stockUnderflowCount = AtomicInteger(0)

        // when: 여러 스레드에서 동시에 재고 1 감소 시도
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    productFacade.decreaseStock(
                        ProductCriteria.DecreaseStock(
                            productId = productId,
                            amount = decreaseAmount
                        )
                    )
                    successCount.incrementAndGet()
                } catch (e: ObjectOptimisticLockingFailureException) {
                    // 낙관적 락 실패 카운트 (재시도 로직이 있다면 여기서 처리 가능)
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: OptimisticLockingFailureException) {
                    // 낙관적 락 실패 카운트 (재시도 로직이 있다면 여기서 처리 가능)
                    optimisticLockFailCount.incrementAndGet()
                } catch (e: ProductException.StockAmountUnderflow) {
                    // 재고 부족 예외 카운트
                    stockUnderflowCount.incrementAndGet()
                } catch (e: Exception) {
                    // 기타 예외 로깅 또는 처리
                    println("예외 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await() // 모든 스레드가 완료될 때까지 대기
        executor.shutdown()

        // then: 최종 재고 확인
        val finalProduct = productFacade.getProduct(ProductCriteria.GetById(productId))
        val expectedStock = initialStock - successCount.get() // 성공한 만큼만 감소

        println("Total Attempts: $numberOfThreads")
        println("Successful Decreases: ${successCount.get()}")
        println("Optimistic Lock Failures: ${optimisticLockFailCount.get()}")
        println("Stock Underflow Errors: ${stockUnderflowCount.get()}")
        println("Expected Final Stock: $expectedStock")
        println("Actual Final Stock: ${finalProduct.stock}")

        assertEquals(expectedStock, finalProduct.stock, "최종 재고는 성공한 감소량만큼 줄어들어야 합니다.")
        // 필요하다면 실패 카운트 검증 추가
        // assertEquals(0, optimisticLockFailCount.get(), "낙관적 락 실패가 없어야 합니다.") // 또는 예상되는 실패 수
        // assertEquals(0, stockUnderflowCount.get(), "재고 부족 예외가 없어야 합니다.")
    }
} 