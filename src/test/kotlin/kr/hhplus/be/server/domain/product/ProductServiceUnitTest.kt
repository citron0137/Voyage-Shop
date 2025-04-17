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
        val actualProduct = productService.getProduct(productId)

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
            productService.getProduct(productId)
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
        val actualProducts = productService.getAllProducts()

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
        val newStock = 20L
        val command = ProductCommand.UpdateStock(
            productId = productId,
            amount = newStock
        )

        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = 10L
        )

        every { productRepository.findByIdWithLock(productId) } returns existingProduct

        // when
        val actualProduct = productService.updateStock(command)

        // then
        verify { productRepository.findByIdWithLock(productId) }
        verify { productRepository.update(any()) }
        assertEquals(newStock, actualProduct.stock)
    }

    @Test
    fun `상품 재고를 감소시킬 수 있다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val decreaseAmount = 5L
        val command = ProductCommand.DecreaseStock(
            productId = productId,
            amount = decreaseAmount
        )
        val initialStock = 10L

        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = initialStock
        )

        every { productRepository.findByIdWithLock(productId) } returns existingProduct

        // when
        val actualProduct = productService.decreaseStock(command)

        // then
        verify { productRepository.findByIdWithLock(productId) }
        verify { productRepository.update(any()) }
        assertEquals(initialStock - decreaseAmount, actualProduct.stock)
    }

    @Test
    fun `상품 재고를 증가시킬 수 있다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val increaseAmount = 5L
        val command = ProductCommand.IncreaseStock(
            productId = productId,
            amount = increaseAmount
        )
        val initialStock = 10L

        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = initialStock
        )

        every { productRepository.findByIdWithLock(productId) } returns existingProduct

        // when
        val actualProduct = productService.increaseStock(command)

        // then
        verify { productRepository.findByIdWithLock(productId) }
        verify { productRepository.update(any()) }
        assertEquals(initialStock + increaseAmount, actualProduct.stock)
    }

    @Test
    fun `재고 감소 시 음수로 감소하면 예외가 발생한다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val command = ProductCommand.DecreaseStock(
            productId = productId,
            amount = 15L
        )
        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = 10L
        )
        every { productRepository.findByIdWithLock(productId) } returns existingProduct

        // when & then
        val exception = assertThrows<ProductException.StockAmountUnderflow> {
            productService.decreaseStock(command)
        }
    }

    @Test
    fun `재고 증가 시 최대치를 초과하면 예외가 발생한다`() {
        // given
        val productId = UUID.randomUUID().toString()
        val command = ProductCommand.IncreaseStock(
            productId = productId,
            amount = Long.MAX_VALUE
        )
        val existingProduct = Product(
            productId = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = 1L
        )
        every { productRepository.findByIdWithLock(productId) } returns existingProduct

        // when & then
        assertThrows<ProductException.StockAmountOverflow> {
            productService.increaseStock(command)
        }
    }
} 
