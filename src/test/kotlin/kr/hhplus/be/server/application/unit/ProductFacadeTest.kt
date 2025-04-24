package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@DisplayName("ProductFacade 테스트")
class ProductFacadeTest {
    
    private lateinit var productService: ProductService
    private lateinit var productFacade: ProductFacade
    
    @BeforeEach
    fun setup() {
        productService = mock()
        productFacade = ProductFacade(productService)
    }
    
    @Test
    @DisplayName("상품을 조회한다")
    fun getProduct() {
        // given
        val productId = "product1"
        val criteria = ProductCriteria.GetById(productId)
        val product = Product(
            productId = productId,
            name = "테스트 상품",
            price = 10000,
            stock = 100
        )
        
        whenever(productService.getProductById(ProductQuery.GetById(productId))).thenReturn(product)
        
        // when
        val result = productFacade.getProduct(criteria)
        
        // then
        assertThat(result.productId).isEqualTo(productId)
        assertThat(result.name).isEqualTo("테스트 상품")
        assertThat(result.price).isEqualTo(10000)
        assertThat(result.stock).isEqualTo(100)
    }
    
    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 예외가 발생한다")
    fun getProductWithNonExistingId() {
        // given
        val productId = "non-existing-id"
        val criteria = ProductCriteria.GetById(productId)
        
        whenever(productService.getProductById(ProductQuery.GetById(productId))).thenThrow(ProductException.NotFound("상품을 찾을 수 없습니다"))
        
        // when, then
        assertThrows<ProductException.NotFound> {
            productFacade.getProduct(criteria)
        }
    }
    
    @Test
    @DisplayName("모든 상품을 조회한다")
    fun getAllProducts() {
        // given
        val criteria = ProductCriteria.GetAll()
        val products = listOf(
            Product(
                productId = "product1",
                name = "테스트 상품 1",
                price = 10000,
                stock = 100
            ),
            Product(
                productId = "product2",
                name = "테스트 상품 2",
                price = 20000,
                stock = 200
            )
        )
        
        whenever(productService.getAllProducts(ProductQuery.GetAll)).thenReturn(products)
        
        // when
        val result = productFacade.getAllProducts(criteria)
        
        // then
        assertThat(result.products).hasSize(2)
        assertThat(result.products[0].productId).isEqualTo("product1")
        assertThat(result.products[0].name).isEqualTo("테스트 상품 1")
        assertThat(result.products[1].productId).isEqualTo("product2")
        assertThat(result.products[1].name).isEqualTo("테스트 상품 2")
    }
    
    @Test
    @DisplayName("상품을 생성한다")
    fun createProduct() {
        // given
        val name = "새 상품"
        val price = 15000L
        val stock = 50L
        val criteria = ProductCriteria.Create(name, price, stock)
        
        val createdProduct = Product(
            productId = "new-product-id",
            name = name,
            price = price,
            stock = stock
        )
        
        whenever(productService.createProduct(any<ProductCommand.Create>())).thenReturn(createdProduct)
        
        // when
        val result = productFacade.createProduct(criteria)
        
        // then
        assertThat(result.productId).isEqualTo("new-product-id")
        assertThat(result.name).isEqualTo(name)
        assertThat(result.price).isEqualTo(price)
        assertThat(result.stock).isEqualTo(stock)
    }
    
    @Test
    @DisplayName("상품 재고를 업데이트한다")
    fun updateStock() {
        // given
        val productId = "product1"
        val newStock = 200L
        val criteria = ProductCriteria.UpdateStock(productId, newStock)
        
        val updatedProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 10000,
            stock = newStock
        )
        
        whenever(productService.updateProductStock(any<ProductCommand.UpdateStock>())).thenReturn(updatedProduct)
        
        // when
        val result = productFacade.updateStock(criteria)
        
        // then
        assertThat(result.productId).isEqualTo(productId)
        assertThat(result.stock).isEqualTo(newStock)
    }
    
    @Test
    @DisplayName("상품 재고를 증가시킨다")
    fun increaseStock() {
        // given
        val productId = "product1"
        val amount = 50L
        val criteria = ProductCriteria.IncreaseStock(productId, amount)
        val initialStock = 100L
        val expectedStock = initialStock + amount
        
        val updatedProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 10000,
            stock = expectedStock
        )
        
        whenever(productService.increaseProductStock(any<ProductCommand.IncreaseStock>())).thenReturn(updatedProduct)
        
        // when
        val result = productFacade.increaseStock(criteria)
        
        // then
        assertThat(result.productId).isEqualTo(productId)
        assertThat(result.stock).isEqualTo(expectedStock)
    }
    
    @Test
    @DisplayName("상품 재고를 감소시킨다")
    fun decreaseStock() {
        // given
        val productId = "product1"
        val amount = 30L
        val criteria = ProductCriteria.DecreaseStock(productId, amount)
        val initialStock = 100L
        val expectedStock = initialStock - amount
        
        val updatedProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 10000,
            stock = expectedStock
        )
        
        whenever(productService.decreaseProductStock(any<ProductCommand.DecreaseStock>())).thenReturn(updatedProduct)
        
        // when
        val result = productFacade.decreaseStock(criteria)
        
        // then
        assertThat(result.productId).isEqualTo(productId)
        assertThat(result.stock).isEqualTo(expectedStock)
    }
} 