package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class ProductService (
    private val repository: ProductRepository
) {

    val MAX_STOCK_AMOUNT = Long.MAX_VALUE

    fun createProduct(command: ProductCommand.Create): Product {
        val productId = UUID.randomUUID().toString()
        if(command.stock >= MAX_STOCK_AMOUNT) throw ProductException.StockAmountOverflow("")
        val product = Product(
            productId = productId,
            name = command.name,
            price = command.price,
            stock = command.stock,
        )
        repository.create(product)
        return product
    }

    fun getProduct(id: String): Product {
        return this.repository.findById(id)
            ?: throw ProductException.NotFound("Product with id: $id")
    }

    fun getAllProducts(): List<Product> {
        return this.repository.findAll()
    }

    fun updateStock(command: ProductCommand.UpdateStock): Product {
        if(command.amount < 0) throw ProductException.StockAmountShouldMoreThan0("")
        if(command.amount >= MAX_STOCK_AMOUNT) throw ProductException.StockAmountOverflow("")
        val product = repository.findById(command.productId)
            ?: throw ProductException.NotFound(command.productId+" is not found")
        product.stock = command.amount
        product.updatedAt = LocalDateTime.now()
        repository.update(product)
        return product
    }

    fun decreaseStock(command: ProductCommand.DecreaseStock): Product {
        val product = repository.findById(command.productId)
            ?: throw ProductException.NotFound(command.productId+" is not found")
        product.stock -= command.amount
        if(product.stock < 0)
            throw ProductException.StockAmountUnderflow(command.productId+" is negative")
        product.updatedAt = LocalDateTime.now()
        repository.update(product)
        return product
    }

    fun increaseStock(command: ProductCommand.IncreaseStock): Product {
        val product = repository.findById(command.productId)
        ?: throw ProductException.NotFound(command.productId+" is not found")
        if( command.amount >= MAX_STOCK_AMOUNT - product.stock)
            throw ProductException.StockAmountOverflow(command.productId+" is more than $MAX_STOCK_AMOUNT")
        product.stock += command.amount
        product.updatedAt = LocalDateTime.now()
        repository.update(product)
        return product
    }
}