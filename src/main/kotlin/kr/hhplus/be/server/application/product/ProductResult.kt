package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.Product
import java.time.LocalDateTime

/**
 * 상품 결과 DTO
 */
data class ProductResult(
    val productId: String,
    val name: String,
    val price: Long,
    val stock: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * Product 도메인 객체를 ProductResult DTO로 변환합니다.
         */
        fun from(product: Product): ProductResult {
            return ProductResult(
                productId = product.productId,
                name = product.name,
                price = product.price,
                stock = product.stock,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}

/**
 * 상품 목록 결과 DTO
 */
data class ProductListResult(
    val products: List<ProductResult>
) {
    companion object {
        /**
         * Product 도메인 객체 목록을 ProductListResult DTO로 변환합니다.
         */
        fun from(products: List<Product>): ProductListResult {
            return ProductListResult(
                products = products.map { ProductResult.from(it) }
            )
        }
    }
} 