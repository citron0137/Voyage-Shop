package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID

class ProductServiceUnitTest {
    private lateinit var productRepository: ProductRepository
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productRepository = mockk(relaxed = true)
        productService = ProductService(productRepository)
    }

    @Test
    fun `상품을 생성할 수 있다`() {
        // given
        val command = ProductCommand.Create(
            name = "테스트 상품",
            price = 1000L,
            stock = 10L
        )
        
        val slot = slot<Product>()
        every { productRepository.create(capture(slot)) } answers {
            slot.captured
        }

        // when
        val actualProduct = productService.createProduct(command)

        // then
        verify { productRepository.create(any()) }
        assert(UUID.fromString(actualProduct.productId) != null) // UUID 형식 검증
        assertEquals(command.name, actualProduct.name)
        assertEquals(command.price, actualProduct.price)
        assertEquals(command.stock, actualProduct.stock)
        assertNotNull(actualProduct.createdAt)
        assertNotNull(actualProduct.updatedAt)
    }

    @Test
    fun `ID로 상품을 조회할 수 있다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val expectedProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = 10L
        )
        every { productRepository.findById(productId) } returns expectedProduct

        // when
        val actualProduct = productService.getProductById(ProductQuery.GetById(productId))

        // then
        verify { productRepository.findById(productId) }
        assertEquals(expectedProduct, actualProduct)
        assertNotNull(actualProduct.createdAt)
        assertNotNull(actualProduct.updatedAt)
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 예외가 발생한다`() {
        // given
        val productId = UUID.randomUUID().toString()
        every { productRepository.findById(productId) } returns null

        // when & then
        assertThrows<ProductException.NotFound> {
            productService.getProductById(ProductQuery.GetById(productId))
        }
    }

    @Test
    fun `모든 상품을 조회할 수 있다`() {
        // given
        val products = listOf(
            Product(
                productId = UUID.randomUUID().toString(),
                name = "상품1",
                price = 1000L,
                stock = 10L
            ),
            Product(
                productId = UUID.randomUUID().toString(),
                name = "상품2",
                price = 2000L,
                stock = 20L
            )
        )
        every { productRepository.findAll() } returns products

        // when
        val actualProducts = productService.getAllProducts(ProductQuery.GetAll)

        // then
        verify { productRepository.findAll() }
        assertEquals(products, actualProducts)
        actualProducts.forEach {
            assertNotNull(it.createdAt)
            assertNotNull(it.updatedAt)
        }
    }

    @Test
    fun `상품 재고를 수정할 수 있다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val initialStock = 10L
        val newStock = 20L
        
        val command = ProductCommand.UpdateStock(
            productId = productId,
            amount = newStock
        )

        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = initialStock
        )
        
        val updatedProduct = existingProduct.copy(
            stock = newStock,
            updatedAt = LocalDateTime.now()
        )

        every { productRepository.findById(productId) } returns existingProduct
        every { productRepository.update(any()) } returns updatedProduct

        // when
        val actualProduct = productService.updateProductStock(command)

        // then
        verify { productRepository.findById(productId) }
        verify { productRepository.update(any()) }
        assertEquals(newStock, actualProduct.stock)
    }

    @Test
    fun `상품 재고를 감소시킬 수 있다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val initialStock = 10L
        val decreaseAmount = 5L
        val finalStock = initialStock - decreaseAmount
        
        val command = ProductCommand.DecreaseStock(
            productId = productId,
            amount = decreaseAmount
        )

        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = initialStock
        )
        
        val updatedProduct = existingProduct.copy(
            stock = finalStock,
            updatedAt = LocalDateTime.now()
        )

        every { productRepository.findById(productId) } returns existingProduct
        every { productRepository.update(any()) } returns updatedProduct

        // when
        val actualProduct = productService.decreaseProductStock(command)

        // then
        verify { productRepository.findById(productId) }
        verify { productRepository.update(any()) }
        assertEquals(finalStock, actualProduct.stock)
    }

    @Test
    fun `상품 재고를 증가시킬 수 있다`() {
        // given
        val productId = "test-product-id"
        val initialStock = 10L
        val increaseAmount = 5L
        val finalStock = initialStock + increaseAmount
        
        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = initialStock
        )
        
        val command = ProductCommand.IncreaseStock(
            productId = productId,
            amount = increaseAmount
        )
        
        val updatedProduct = existingProduct.copy(
            stock = finalStock,
            updatedAt = LocalDateTime.now()
        )
        
        every { productRepository.findById(productId) } returns existingProduct
        every { productRepository.update(any()) } returns updatedProduct
        
        // when
        val actualProduct = productService.increaseProductStock(command)
        
        // then
        verify { productRepository.findById(productId) }
        verify { productRepository.update(any()) }
        assertEquals(finalStock, actualProduct.stock)
    }

    @Test
    fun `재고 감소 시 음수로 감소하면 예외가 발생한다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val initialStock = 10L
        val decreaseAmount = 15L // 현재 재고보다 큰 값
        
        val command = ProductCommand.DecreaseStock(
            productId = productId,
            amount = decreaseAmount
        )
        
        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = initialStock
        )

        val finalStock = initialStock - decreaseAmount
        val updatedProduct = existingProduct.copy(
            stock = finalStock,
            updatedAt = LocalDateTime.now()
        )
        
        every { productRepository.findById(productId) } returns existingProduct
        every { productRepository.update(any()) } returns updatedProduct

        // when & then
        assertThrows<ProductException.StockAmountUnderflow> {
            productService.decreaseProductStock(command)
        }
        
        // update 호출 안됨 검증
        verify(exactly = 0) { productRepository.update(any()) }
    }

    @Test
    fun `재고 증가 시 최대치를 초과하면 예외가 발생한다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val initialStock = 1L
        val increaseAmount = Long.MAX_VALUE
        
        val command = ProductCommand.IncreaseStock(
            productId = productId,
            amount = increaseAmount
        )
        
        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = initialStock
        )
        
        every { productRepository.findById(productId) } returns existingProduct

        // when & then
        assertThrows<ProductException.StockAmountOverflow> {
            productService.increaseProductStock(command)
        }
        
        // update 호출 안됨 검증
        verify(exactly = 0) { productRepository.update(any()) }
    }
} 
