package kr.hhplus.be.server.domain.product

/**
 * 상품 도메인 명령 관련 클래스
 */
sealed class ProductCommand {
    /**
     * 상품 생성 명령
     */
    data class Create(
        val name: String,
        val price: Long,
        val stock: Long,
    ) : ProductCommand() {
        init {
            if (name.isBlank()) throw ProductException.NameShouldNotBlank("상품명은 비어있을 수 없습니다.")
            if (price <= 0) throw ProductException.PriceShouldMoreThan0("가격은 0보다 커야합니다.")
            if (stock < 0) throw ProductException.StockAmountShouldMoreThan0("재고는 0보다 크거나 같아야 합니다.")
        }
    }

    /**
     * 상품 재고 감소 명령
     */
    data class DecreaseStock(
        val productId: String,
        val amount: Long,
    ) : ProductCommand() {
        init {
            if (productId.isBlank()) throw ProductException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount <= 0) throw ProductException.DecreaseStockAmountShouldMoreThan0("감소량은 0보다 커야합니다.")
        }
    }

    /**
     * 상품 재고 증가 명령
     */
    data class IncreaseStock(
        val productId: String,
        val amount: Long,
    ) : ProductCommand() {
        init {
            if (productId.isBlank()) throw ProductException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount <= 0) throw ProductException.IncreaseStockAmountShouldMoreThan0("증가량은 0보다 커야합니다.")
        }
    }

    /**
     * 상품 재고 갱신 명령
     */
    data class UpdateStock(
        val productId: String,
        val amount: Long,
    ) : ProductCommand() {
        init {
            if (productId.isBlank()) throw ProductException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount < 0) throw ProductException.StockAmountShouldMoreThan0("재고는 0보다 크거나 같아야 합니다.")
        }
    }
}
