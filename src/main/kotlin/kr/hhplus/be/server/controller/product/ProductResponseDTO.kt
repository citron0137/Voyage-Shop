package kr.hhplus.be.server.controller.product.response

import kr.hhplus.be.server.application.product.ProductListResult
import kr.hhplus.be.server.application.product.ProductResult
import java.time.LocalDateTime

/**
 * 상품 응답 DTO
 */
sealed class ProductResponseDTO {
    /**
     * 단일 상품 응답 DTO
     */
    data class Single(
        val productId: String,
        val name: String,
        val price: Long,
        val stock: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) : ProductResponseDTO() {
        companion object {
            /**
             * ProductResult를 ProductResponseDTO.Single로 변환합니다.
             */
            fun from(result: ProductResult): Single {
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
     * 상품 목록 응답 DTO
     */
    data class List(
        val products: kotlin.collections.List<Single>
    ) : ProductResponseDTO() {
        companion object {
            /**
             * ProductListResult를 ProductResponseDTO.List로 변환합니다.
             */
            fun from(result: ProductListResult): List {
                return List(
                    products = result.products.map { Single.from(it) }
                )
            }
        }
    }
} 