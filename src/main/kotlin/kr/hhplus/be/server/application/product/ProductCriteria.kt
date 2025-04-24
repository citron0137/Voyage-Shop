package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.ProductCommand
import kr.hhplus.be.server.domain.product.ProductQuery

/**
 * 상품 관련 요청 기준을 담는 클래스
 */
class ProductCriteria {
    /**
     * 상품 조회 요청
     */
    data class GetById(
        val productId: String
    ) {
        fun toQuery(): ProductQuery.GetById {
            return ProductQuery.GetById(productId)
        }

        init {
            require(productId.isNotBlank()) { "상품 ID는 비어있을 수 없습니다." }
        }
    }

    /**
     * 모든 상품 조회 요청
     */
    class GetAll {
        fun toQuery(): ProductQuery.GetAll {
            return ProductQuery.GetAll
        }
    }

    /**
     * 상품 생성 요청
     */
    data class Create(
        val name: String,
        val price: Long,
        val stock: Long
    ) {
        fun toCommand(): ProductCommand.Create {
            return ProductCommand.Create(
                name = name,
                price = price,
                stock = stock
            )
        }

        init {
            require(name.isNotBlank()) { "상품명은 비어있을 수 없습니다." }
            require(price > 0) { "가격은 0보다 커야 합니다." }
            require(stock >= 0) { "재고는 0 이상이어야 합니다." }
        }
    }

    /**
     * 상품 재고 업데이트 요청
     */
    data class UpdateStock(
        val productId: String,
        val stock: Long
    ) {
        fun toCommand(): ProductCommand.UpdateStock {
            return ProductCommand.UpdateStock(
                productId = productId,
                amount = stock
            )
        }

        init {
            require(productId.isNotBlank()) { "상품 ID는 비어있을 수 없습니다." }
            require(stock >= 0) { "재고는 0 이상이어야 합니다." }
        }
    }

    /**
     * 상품 재고 증가 요청
     */
    data class IncreaseStock(
        val productId: String,
        val amount: Long
    ) {
        fun toCommand(): ProductCommand.IncreaseStock {
            return ProductCommand.IncreaseStock(
                productId = productId,
                amount = amount
            )
        }

        init {
            require(productId.isNotBlank()) { "상품 ID는 비어있을 수 없습니다." }
            require(amount > 0) { "증가량은 0보다 커야 합니다." }
        }
    }

    /**
     * 상품 재고 감소 요청
     */
    data class DecreaseStock(
        val productId: String,
        val amount: Long
    ) {
        fun toCommand(): ProductCommand.DecreaseStock {
            return ProductCommand.DecreaseStock(
                productId = productId,
                amount = amount
            )
        }

        init {
            require(productId.isNotBlank()) { "상품 ID는 비어있을 수 없습니다." }
            require(amount > 0) { "감소량은 0보다 커야 합니다." }
        }
    }
} 