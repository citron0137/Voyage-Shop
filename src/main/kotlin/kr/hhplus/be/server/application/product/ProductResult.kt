package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.Product
import java.time.LocalDateTime

/**
 * 상품 관련 응답 결과를 담는 클래스
 */
class ProductResult {
    /**
     * 단일 상품 정보 응답
     */
    data class Single(
        val productId: String,
        val name: String,
        val price: Long,
        val stock: Long,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            /**
             * 도메인 Product 객체를 ProductResult.Single DTO로 변환합니다.
             */
            fun from(product: kr.hhplus.be.server.domain.product.Product): Single {
                return Single(
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
     * 상품 목록 응답
     */
    data class List(
        val products: kotlin.collections.List<Single>
    ) {
        companion object {
            /**
             * 도메인 Product 객체 목록을 ProductResult.List DTO로 변환합니다.
             */
            fun from(products: kotlin.collections.List<kr.hhplus.be.server.domain.product.Product>): List {
                return List(
                    products = products.map { Single.from(it) }
                )
            }
        }
    }
} 