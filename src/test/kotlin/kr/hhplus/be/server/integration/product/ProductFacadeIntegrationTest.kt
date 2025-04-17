package kr.hhplus.be.server.integration.product

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.domain.product.ProductException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@DisplayName("상품 Facade 통합 테스트")
class ProductFacadeIntegrationTest {

    @Autowired
    private lateinit var productFacade: ProductFacade

    @Test
    @DisplayName("새로운 상품을 생성할 수 있다")
    fun createProductTest() {
        // given
        val criteria = ProductCriteria.Create(
            name = "테스트 상품",
            price = 10000,
            stock = 100
        )

        // when
        val product = productFacade.createProduct(criteria)

        // then
        assertThat(product.name).isEqualTo("테스트 상품")
        assertThat(product.price).isEqualTo(10000)
        assertThat(product.stock).isEqualTo(100)
    }

    @Test
    @DisplayName("상품 ID로 상품을 조회할 수 있다")
    fun getProductByIdTest() {
        // given
        val createCriteria = ProductCriteria.Create(
            name = "조회용 상품",
            price = 5000,
            stock = 50
        )
        val createdProduct = productFacade.createProduct(createCriteria)

        val getCriteria = ProductCriteria.GetById(createdProduct.productId)

        // when
        val product = productFacade.getProduct(getCriteria)

        // then
        assertThat(product.productId).isEqualTo(createdProduct.productId)
        assertThat(product.name).isEqualTo("조회용 상품")
        assertThat(product.price).isEqualTo(5000)
        assertThat(product.stock).isEqualTo(50)
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회하면 예외가 발생한다")
    fun getProductNotFoundTest() {
        // given
        val nonExistentProductId = "non-existent-id"
        val getCriteria = ProductCriteria.GetById(nonExistentProductId)

        // when & then
        assertThrows<ProductException.NotFound> {
            productFacade.getProduct(getCriteria)
        }
    }

    @Test
    @DisplayName("모든 상품을 조회할 수 있다")
    fun getAllProductsTest() {
        // given
        val initialProducts = productFacade.getAllProducts()
        val initialSize = initialProducts.products.size
        
        // 테스트용 상품 추가
        productFacade.createProduct(ProductCriteria.Create("상품1", 1000, 10))
        productFacade.createProduct(ProductCriteria.Create("상품2", 2000, 20))
        productFacade.createProduct(ProductCriteria.Create("상품3", 3000, 30))
        
        // when
        val products = productFacade.getAllProducts()
        
        // then
        assertThat(products.products.size).isEqualTo(initialSize + 3)
        assertThat(products.products.map { it.name }).contains("상품1", "상품2", "상품3")
    }

    @Test
    @DisplayName("상품 재고를 갱신할 수 있다")
    fun updateProductStockTest() {
        // given
        val createCriteria = ProductCriteria.Create("재고 갱신 테스트 상품", 15000, 150)
        val createdProduct = productFacade.createProduct(createCriteria)
        
        val updateStockCriteria = ProductCriteria.UpdateStock(
            productId = createdProduct.productId,
            stock = 200
        )
        
        // when
        val updatedProduct = productFacade.updateStock(updateStockCriteria)
        
        // then
        assertThat(updatedProduct.stock).isEqualTo(200)
    }

    @Test
    @DisplayName("상품 재고를 음수로 갱신하면 예외가 발생한다")
    fun updateProductStockWithNegativeAmountTest() {
        // given
        val createCriteria = ProductCriteria.Create("음수 재고 테스트 상품", 25000, 250)
        val createdProduct = productFacade.createProduct(createCriteria)
        
        val negativeStockCriteria = ProductCriteria.UpdateStock(
            productId = createdProduct.productId,
            stock = -10
        )
        
        // when & then
        assertThrows<ProductException.StockAmountShouldMoreThan0> {
            productFacade.updateStock(negativeStockCriteria)
        }
    }

    @Test
    @DisplayName("상품 재고를 감소시킬 수 있다")
    fun decreaseProductStockTest() {
        // given
        val createCriteria = ProductCriteria.Create("재고 감소 테스트 상품", 30000, 300)
        val createdProduct = productFacade.createProduct(createCriteria)
        
        val decreaseStockCriteria = ProductCriteria.DecreaseStock(
            productId = createdProduct.productId,
            amount = 50
        )
        
        // when
        val updatedProduct = productFacade.decreaseStock(decreaseStockCriteria)
        
        // then
        assertThat(updatedProduct.stock).isEqualTo(250)
    }

    @Test
    @DisplayName("상품 재고보다 많은 양을 감소시키면 예외가 발생한다")
    fun decreaseProductStockWithExcessiveAmountTest() {
        // given
        val createCriteria = ProductCriteria.Create("재고 초과 감소 테스트 상품", 40000, 40)
        val createdProduct = productFacade.createProduct(createCriteria)
        
        val excessiveDecreaseCriteria = ProductCriteria.DecreaseStock(
            productId = createdProduct.productId,
            amount = 50
        )
        
        // when & then
        assertThrows<ProductException.StockAmountUnderflow> {
            productFacade.decreaseStock(excessiveDecreaseCriteria)
        }
    }

    @Test
    @DisplayName("상품 재고를 증가시킬 수 있다")
    fun increaseProductStockTest() {
        // given
        val createCriteria = ProductCriteria.Create("재고 증가 테스트 상품", 50000, 500)
        val createdProduct = productFacade.createProduct(createCriteria)
        
        val increaseStockCriteria = ProductCriteria.IncreaseStock(
            productId = createdProduct.productId,
            amount = 100
        )
        
        // when
        val updatedProduct = productFacade.increaseStock(increaseStockCriteria)
        
        // then
        assertThat(updatedProduct.stock).isEqualTo(600)
    }

    @Test
    @Transactional
    @DisplayName("여러 스레드에서 동시에 상품 재고를 감소시켜도 정확한 재고가 유지된다")
    fun decreaseProductStockConcurrencyTest() {
        // given: 테스트 상품 생성
        val createCriteria = ProductCriteria.Create("동시성 테스트 상품", 1000, 1000)
        val product = productFacade.createProduct(createCriteria)
        
        // given: 동시 감소 설정
        val threadCount = 10
        val decreaseAmount = 10L
        val expectedRemainingStock = product.stock - (threadCount * decreaseAmount)
        
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        // when: 여러 스레드에서 동시에 재고 감소 수행
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    val decreaseCriteria = ProductCriteria.DecreaseStock(product.productId, decreaseAmount)
                    productFacade.decreaseStock(decreaseCriteria)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 마칠 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        // then: 최종 재고 확인
        val updatedProduct = productFacade.getProduct(ProductCriteria.GetById(product.productId))
        
        // 모든 감소가 정확히 반영되었는지 검증
        assertThat(updatedProduct.stock).isEqualTo(expectedRemainingStock)
    }
} 