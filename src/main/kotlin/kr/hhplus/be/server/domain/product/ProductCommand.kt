package kr.hhplus.be.server.domain.product

class ProductCommand {
    class Create(
        val name: String,
        val price: Long,
        val stock: Long,
    ) {
        init {
            if (name.isBlank()) throw ProductException.NameShouldNotBlank("상품명은 비어있을 수 없습니다.")
            if (price <= 0) throw ProductException.PriceShouldMoreThan0("가격은 0보다 커야합니다.")
            if (stock < 0) throw ProductException.StockAmountShouldMoreThan0("재고는 0보다 크거나 같아야 합니다.")
        }
    }

    class DecreaseStock(
        val productId: String,
        val amount: Long,
    ) {
        init {
            if (productId.isBlank()) throw ProductException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount <= 0) throw ProductException.DecreaseStockAmountShouldMoreThan0("감소량은 0보다 커야합니다.")
        }
    }

    class IncreaseStock(
        val productId: String,
        val amount: Long,
    ) {
        init {
            if (productId.isBlank()) throw ProductException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount <= 0) throw ProductException.IncreaseStockAmountShouldMoreThan0("증가량은 0보다 커야합니다.")
        }
    }

    class UpdateStock(
        val productId: String,
        val amount: Long,
    ) {
        init {
            if (productId.isBlank()) throw ProductException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
            if (amount < 0) throw ProductException.StockAmountShouldMoreThan0("재고는 0보다 크거나 같아야 합니다.")
        }
    }
}
