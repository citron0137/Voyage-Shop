package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime
import java.util.UUID

data class Product (
    val productId: String,
    val name: String,
    val price: Long,
    val stock: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        val MAX_STOCK_AMOUNT = Long.MAX_VALUE
        
        fun create(name: String, price: Long, stock: Long): Product {
            return Product(
                productId = UUID.randomUUID().toString(),
                name = name,
                price = price,
                stock = stock
            )
        }
    }
    
    /**
     * 상품 재고를 감소시킵니다.
     *
     * @param amount 감소시킬 수량
     * @return 재고가 감소된 새 상품 객체
     * @throws ProductException.StockAmountUnderflow 재고가 부족한 경우
     */
    fun decreaseStock(amount: Long): Product {
        if (stock < amount) {
            throw ProductException.StockAmountUnderflow("Stock amount underflow for product: $productId")
        }
        
        return this.copy(
            stock = stock - amount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 상품 재고를 증가시킵니다.
     *
     * @param amount 증가시킬 수량
     * @return 재고가 증가된 새 상품 객체
     * @throws ProductException.StockAmountOverflow 재고가 최대치를 초과하는 경우
     */
    fun increaseStock(amount: Long): Product {
        if (amount >= MAX_STOCK_AMOUNT - stock) {
            throw ProductException.StockAmountOverflow("Stock amount overflow for product: $productId")
        }
        
        return this.copy(
            stock = stock + amount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 상품 재고를 특정 수량으로 갱신합니다.
     *
     * @param amount 설정할 재고 수량
     * @return 재고가 갱신된 새 상품 객체
     */
    fun updateStock(amount: Long): Product {
        return this.copy(
            stock = amount,
            updatedAt = LocalDateTime.now()
        )
    }
}