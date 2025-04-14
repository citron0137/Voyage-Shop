package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class ProductService (
    private val repository: ProductRepository
) {

    val MAX_STOCK_AMOUNT = Long.MAX_VALUE

    /**
     * 상품 생성 명령을 처리합니다.
     *
     * @param command 상품 생성 명령
     * @return 생성된 상품
     * @throws ProductException.StockAmountOverflow 재고가 최대치를 초과한 경우
     */
    fun handle(command: ProductCommand.Create): Product {
        return createProduct(command)
    }

    /**
     * ID로 상품 조회 쿼리를 처리합니다.
     *
     * @param query ID로 상품 조회 쿼리
     * @return 조회된 상품
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     */
    fun handle(query: ProductQuery.GetById): Product {
        return getProduct(query.productId)
    }

    /**
     * 모든 상품 조회 쿼리를 처리합니다.
     *
     * @param query 모든 상품 조회 쿼리
     * @return 모든 상품 목록
     */
    fun handle(query: ProductQuery.GetAll): List<Product> {
        return getAllProducts()
    }

    /**
     * 상품 재고 갱신 명령을 처리합니다.
     *
     * @param command 상품 재고 갱신 명령
     * @return 갱신된 상품
     * @throws ProductException.StockAmountShouldMoreThan0 재고가 0보다 작은 경우
     * @throws ProductException.StockAmountOverflow 재고가 최대치를 초과한 경우
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     */
    fun handle(command: ProductCommand.UpdateStock): Product {
        return updateStock(command)
    }

    /**
     * 상품 재고 감소 명령을 처리합니다.
     *
     * @param command 상품 재고 감소 명령
     * @return 갱신된 상품
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.StockAmountUnderflow 재고가 음수인 경우
     */
    fun handle(command: ProductCommand.DecreaseStock): Product {
        return decreaseStock(command)
    }

    /**
     * 상품 재고 증가 명령을 처리합니다.
     *
     * @param command 상품 재고 증가 명령
     * @return 갱신된 상품
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.StockAmountOverflow 재고가 최대치를 초과한 경우
     */
    fun handle(command: ProductCommand.IncreaseStock): Product {
        return increaseStock(command)
    }

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