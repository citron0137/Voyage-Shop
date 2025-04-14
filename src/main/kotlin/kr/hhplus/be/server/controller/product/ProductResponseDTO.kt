package kr.hhplus.be.server.controller.product

import kr.hhplus.be.server.application.product.ProductResult
import java.time.LocalDateTime

/**
 * 상품 응답
 */
sealed class ProductResponse {
    /**
     * 단일 상품 응답
     */
    data class Single(
        val productId: String,
        val name: String,
        val price: Long,
        val stock: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : ProductResponse() {
        companion object {
            /**
             * ProductResult.Product를 ProductResponse.Single로 변환합니다.
             */
            fun from(result: ProductResult.Product): Single {
                return Single(
                    productId = result.productId,
                    name = result.name,
                    price = result.price,
                    stock = result.stock,
                    createdAt = result.createdAt,
                    updatedAt = result.updatedAt
                )
            }
        }
    }
    
    /**
     * 상품 목록 응답
     */
    data class List(
        val products: kotlin.collections.List<Single>
    ) : ProductResponse() {
        companion object {
            /**
             * ProductResult.List를 ProductResponse.List로 변환합니다.
             */
            fun from(result: ProductResult.List): List {
                return List(
                    products = result.products.map { Single.from(it) }
                )
            }
        }
    }
} 