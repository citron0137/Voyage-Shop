package kr.hhplus.be.server.domain.product

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductCommandTest {

    @Test
    @DisplayName("Create 커맨드를 정상적으로 생성할 수 있다")
    fun `Create 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val name = "테스트 상품"
        val price = 1000L
        val stock = 10L
        
        // when
        val command = ProductCommand.Create(name, price, stock)
        
        // then
        assertEquals(name, command.name)
        assertEquals(price, command.price)
        assertEquals(stock, command.stock)
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 재고가 음수면 예외가 발생한다")
    fun `Create 커맨드 생성 시 재고가 음수면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.StockAmountShouldMoreThan0> {
            ProductCommand.Create("테스트 상품", 1000L, -1L)
        }
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 가격이 0 이하면 예외가 발생한다")
    fun `Create 커맨드 생성 시 가격이 0 이하면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.PriceShouldMoreThan0> {
            ProductCommand.Create("테스트 상품", 0L, 10L)
        }
        
        assertThrows<ProductException.PriceShouldMoreThan0> {
            ProductCommand.Create("테스트 상품", -1L, 10L)
        }
    }
    
    @Test
    @DisplayName("Create 커맨드 생성 시 이름이 비어있으면 예외가 발생한다")
    fun `Create 커맨드 생성 시 이름이 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.NameShouldNotBlank> {
            ProductCommand.Create("", 1000L, 10L)
        }
    }
    
    @Test
    @DisplayName("UpdateStock 커맨드를 정상적으로 생성할 수 있다")
    fun `UpdateStock 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val productId = "test-product-id"
        val amount = 10L
        
        // when
        val command = ProductCommand.UpdateStock(productId, amount)
        
        // then
        assertEquals(productId, command.productId)
        assertEquals(amount, command.amount)
    }
    
    @Test
    @DisplayName("UpdateStock 커맨드 생성 시 상품ID가 비어있으면 예외가 발생한다")
    fun `UpdateStock 커맨드 생성 시 상품ID가 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.ProductIdShouldNotBlank> {
            ProductCommand.UpdateStock("", 10L)
        }
    }
    
    @Test
    @DisplayName("UpdateStock 커맨드 생성 시 음수 재고량이면 예외가 발생한다")
    fun `UpdateStock 커맨드 생성 시 음수 재고량이면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.StockAmountShouldMoreThan0> {
            ProductCommand.UpdateStock("test-product-id", -1L)
        }
    }
    
    @Test
    @DisplayName("IncreaseStock 커맨드를 정상적으로 생성할 수 있다")
    fun `IncreaseStock 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val productId = "test-product-id"
        val amount = 10L
        
        // when
        val command = ProductCommand.IncreaseStock(productId, amount)
        
        // then
        assertEquals(productId, command.productId)
        assertEquals(amount, command.amount)
    }
    
    @Test
    @DisplayName("IncreaseStock 커맨드 생성 시 상품ID가 비어있으면 예외가 발생한다")
    fun `IncreaseStock 커맨드 생성 시 상품ID가 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.ProductIdShouldNotBlank> {
            ProductCommand.IncreaseStock("", 10L)
        }
    }
    
    @Test
    @DisplayName("IncreaseStock 커맨드 생성 시 0 이하 증가량이면 예외가 발생한다")
    fun `IncreaseStock 커맨드 생성 시 0 이하 증가량이면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.IncreaseStockAmountShouldMoreThan0> {
            ProductCommand.IncreaseStock("test-product-id", 0L)
        }
        
        assertThrows<ProductException.IncreaseStockAmountShouldMoreThan0> {
            ProductCommand.IncreaseStock("test-product-id", -1L)
        }
    }
    
    @Test
    @DisplayName("DecreaseStock 커맨드를 정상적으로 생성할 수 있다")
    fun `DecreaseStock 커맨드를 정상적으로 생성할 수 있다`() {
        // given
        val productId = "test-product-id"
        val amount = 10L
        
        // when
        val command = ProductCommand.DecreaseStock(productId, amount)
        
        // then
        assertEquals(productId, command.productId)
        assertEquals(amount, command.amount)
    }
    
    @Test
    @DisplayName("DecreaseStock 커맨드 생성 시 상품ID가 비어있으면 예외가 발생한다")
    fun `DecreaseStock 커맨드 생성 시 상품ID가 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.ProductIdShouldNotBlank> {
            ProductCommand.DecreaseStock("", 10L)
        }
    }
    
    @Test
    @DisplayName("DecreaseStock 커맨드 생성 시 0 이하 감소량이면 예외가 발생한다")
    fun `DecreaseStock 커맨드 생성 시 0 이하 감소량이면 예외가 발생한다`() {
        // when & then
        assertThrows<ProductException.DecreaseStockAmountShouldMoreThan0> {
            ProductCommand.DecreaseStock("test-product-id", 0L)
        }
        
        assertThrows<ProductException.DecreaseStockAmountShouldMoreThan0> {
            ProductCommand.DecreaseStock("test-product-id", -1L)
        }
    }
} 