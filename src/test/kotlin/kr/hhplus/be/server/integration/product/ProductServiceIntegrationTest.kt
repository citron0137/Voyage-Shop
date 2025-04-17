package kr.hhplus.be.server.integration.product

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductCommand
import kr.hhplus.be.server.domain.product.ProductException
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.product.ProductService
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
@DisplayName("상품 서비스 통합 테스트")
class ProductServiceIntegrationTest {

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    @DisplayName("상품을 생성할 수 있다")
    fun `상품을 생성할 수 있다`() {
        // given
        val createCommand = ProductCommand.Create(
            name = "테스트 상품",
            price = 10000,
            stock = 100
        )

        // when
        val product = productService.handle(createCommand)

        // then
        assertThat(product.name).isEqualTo("테스트 상품")
        assertThat(product.price).isEqualTo(10000)
        assertThat(product.stock).isEqualTo(100)
        
        // 저장소에서 조회해도 동일한 결과인지 확인
        val savedProduct = productRepository.findById(product.productId)
        assertThat(savedProduct).isNotNull
        assertThat(savedProduct?.name).isEqualTo("테스트 상품")
        assertThat(savedProduct?.price).isEqualTo(10000)
        assertThat(savedProduct?.stock).isEqualTo(100)
    }

    @Test
    @DisplayName("상품 ID로 상품을 조회할 수 있다")
    fun `상품 ID로 상품을 조회할 수 있다`() {
        // given
        val createCommand = ProductCommand.Create(
            name = "조회용 상품",
            price = 5000,
            stock = 50
        )
        val createdProduct = productService.handle(createCommand)

        // when
        val product = productService.handle(ProductQuery.GetById(createdProduct.productId))

        // then
        assertThat(product.productId).isEqualTo(createdProduct.productId)
        assertThat(product.name).isEqualTo("조회용 상품")
        assertThat(product.price).isEqualTo(5000)
        assertThat(product.stock).isEqualTo(50)
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회하면 예외가 발생한다")
    fun `존재하지 않는 상품 ID로 조회하면 예외가 발생한다`() {
        // given
        val nonExistentProductId = "non-existent-id"

        // when & then
        assertThrows<ProductException.NotFound> {
            productService.handle(ProductQuery.GetById(nonExistentProductId))
        }
    }

    @Test
    @DisplayName("모든 상품을 조회할 수 있다")
    fun `모든 상품을 조회할 수 있다`() {
        // given
        val initialProducts = productService.handle(ProductQuery.GetAll())
        val initialSize = initialProducts.size
        
        // 테스트용 상품 추가
        productService.handle(ProductCommand.Create("상품1", 1000, 10))
        productService.handle(ProductCommand.Create("상품2", 2000, 20))
        productService.handle(ProductCommand.Create("상품3", 3000, 30))
        
        // when
        val products = productService.handle(ProductQuery.GetAll())
        
        // then
        assertThat(products.size).isEqualTo(initialSize + 3)
        assertThat(products.map { it.name }).contains("상품1", "상품2", "상품3")
    }

    @Test
    @DisplayName("상품 재고를 갱신할 수 있다")
    fun `상품 재고를 갱신할 수 있다`() {
        // given
        val createCommand = ProductCommand.Create("재고 갱신 테스트 상품", 15000, 150)
        val createdProduct = productService.handle(createCommand)
        
        val updateStockCommand = ProductCommand.UpdateStock(
            productId = createdProduct.productId,
            amount = 200
        )
        
        // when
        val updatedProduct = productService.handle(updateStockCommand)
        
        // then
        assertThat(updatedProduct.stock).isEqualTo(200)
        
        // 저장소에서 조회해도 동일한 결과인지 확인
        val savedProduct = productRepository.findById(createdProduct.productId)
        assertThat(savedProduct?.stock).isEqualTo(200)
    }

    @Test
    @DisplayName("상품 재고를 음수로 갱신하면 예외가 발생한다")
    fun `상품 재고를 음수로 갱신하면 예외가 발생한다`() {
        // given
        val createCommand = ProductCommand.Create("음수 재고 테스트 상품", 25000, 250)
        val createdProduct = productService.handle(createCommand)
        
        val negativeStockCommand = ProductCommand.UpdateStock(
            productId = createdProduct.productId,
            amount = -10
        )
        
        // when & then
        assertThrows<ProductException.StockAmountShouldMoreThan0> {
            productService.handle(negativeStockCommand)
        }
    }

    @Test
    @DisplayName("상품 재고를 감소시킬 수 있다")
    fun `상품 재고를 감소시킬 수 있다`() {
        // given
        val createCommand = ProductCommand.Create("재고 감소 테스트 상품", 30000, 300)
        val createdProduct = productService.handle(createCommand)
        
        val decreaseStockCommand = ProductCommand.DecreaseStock(
            productId = createdProduct.productId,
            amount = 50
        )
        
        // when
        val updatedProduct = productService.handle(decreaseStockCommand)
        
        // then
        assertThat(updatedProduct.stock).isEqualTo(250)
        
        // 저장소에서 조회해도 동일한 결과인지 확인
        val savedProduct = productRepository.findById(createdProduct.productId)
        assertThat(savedProduct?.stock).isEqualTo(250)
    }

    @Test
    @DisplayName("상품 재고보다 많은 양을 감소시키면 예외가 발생한다")
    fun `상품 재고보다 많은 양을 감소시키면 예외가 발생한다`() {
        // given
        val createCommand = ProductCommand.Create("재고 초과 감소 테스트 상품", 40000, 40)
        val createdProduct = productService.handle(createCommand)
        
        val excessiveDecreaseCommand = ProductCommand.DecreaseStock(
            productId = createdProduct.productId,
            amount = 50
        )
        
        // when & then
        assertThrows<ProductException.StockAmountUnderflow> {
            productService.handle(excessiveDecreaseCommand)
        }
    }

    @Test
    @DisplayName("상품 재고를 증가시킬 수 있다")
    fun `상품 재고를 증가시킬 수 있다`() {
        // given
        val createCommand = ProductCommand.Create("재고 증가 테스트 상품", 50000, 500)
        val createdProduct = productService.handle(createCommand)
        
        val increaseStockCommand = ProductCommand.IncreaseStock(
            productId = createdProduct.productId,
            amount = 100
        )
        
        // when
        val updatedProduct = productService.handle(increaseStockCommand)
        
        // then
        assertThat(updatedProduct.stock).isEqualTo(600)
        
        // 저장소에서 조회해도 동일한 결과인지 확인
        val savedProduct = productRepository.findById(createdProduct.productId)
        assertThat(savedProduct?.stock).isEqualTo(600)
    }

    @Test
    @DisplayName("상품 재고 감소에 대한 동시성 테스트")
    fun `여러 스레드에서 동시에 상품 재고를 감소시켜도 정확한 재고가 유지된다`() {
        // given: 테스트 상품 생성
        val createCommand = ProductCommand.Create("동시성 테스트 상품", 1000, 1000)
        val product = productService.handle(createCommand)
        
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
                    val decreaseCommand = ProductCommand.DecreaseStock(product.productId, decreaseAmount)
                    productService.handle(decreaseCommand)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 마칠 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        // then: 최종 재고 확인
        val updatedProduct = productService.handle(ProductQuery.GetById(product.productId))
        
        // 모든 감소가 정확히 반영되었는지 검증
        assertThat(updatedProduct.stock).isEqualTo(expectedRemainingStock)
    }
}

// ProductQuery 클래스 - 테스트에서 사용
sealed class ProductQuery {
    data class GetById(val productId: String) : ProductQuery()
    object GetAll : ProductQuery()
} 