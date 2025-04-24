package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ProductService (
    private val repository: ProductRepository
) {

    /**
     * 상품을 생성합니다.
     *
     * @param command 상품 생성 명령
     * @return 생성된 상품
     */
    fun createProduct(command: ProductCommand.Create): Product {
        val product = command.toProduct()
        return repository.create(product)
    }

    /**
     * ID로 상품을 조회합니다.
     *
     * @param query ID로 상품 조회 쿼리
     * @return 조회된 상품
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getProductById(query: ProductQuery.GetById): Product {
        return repository.findById(query.productId)
            ?: throw ProductException.NotFound("Product with id: ${query.productId}")
    }

    /**
     * 모든 상품을 조회합니다.
     *
     * @param query 모든 상품 조회 쿼리
     * @return 모든 상품 목록
     */
    @Transactional(readOnly = true)
    fun getAllProducts(query: ProductQuery.GetAll): List<Product> {
        return repository.findAll()
    }

    /**
     * 상품 재고를 특정 수량으로 갱신합니다.
     * 비관적 락을 사용하여 동시성 문제를 방지합니다.
     *
     * @param command 상품 재고 갱신 명령
     * @return 갱신된 상품
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     */
    @Transactional
    fun updateProductStock(command: ProductCommand.UpdateStock): Product {
        val product = repository.findByIdWithLock(command.productId)
            ?: throw ProductException.NotFound("Product with id: ${command.productId} not found")
        
        val updatedProduct = product.updateStock(command.amount)
        return repository.update(updatedProduct)
    }

    /**
     * 상품 재고를 감소시킵니다.
     * 비관적 락을 사용하여 동시성 문제를 방지합니다.
     *
     * @param command 상품 재고 감소 명령
     * @return 갱신된 상품
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.StockAmountUnderflow 재고가 음수인 경우
     */
    @Transactional
    fun decreaseProductStock(command: ProductCommand.DecreaseStock): Product {
        val product = repository.findByIdWithLock(command.productId)
            ?: throw ProductException.NotFound("Product with id: ${command.productId} not found")
        
        val updatedProduct = product.decreaseStock(command.amount)
        return repository.update(updatedProduct)
    }

    /**
     * 상품 재고를 증가시킵니다.
     * 비관적 락을 사용하여 동시성 문제를 방지합니다.
     *
     * @param command 상품 재고 증가 명령
     * @return 갱신된 상품
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.StockAmountOverflow 재고가 최대치를 초과한 경우
     */
    @Transactional
    fun increaseProductStock(command: ProductCommand.IncreaseStock): Product {
        val product = repository.findByIdWithLock(command.productId)
            ?: throw ProductException.NotFound("Product with id: ${command.productId} not found")
        
        val updatedProduct = product.increaseStock(command.amount)
        return repository.update(updatedProduct)
    }
}