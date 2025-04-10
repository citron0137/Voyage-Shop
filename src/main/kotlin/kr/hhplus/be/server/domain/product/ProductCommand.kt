package kr.hhplus.be.server.domain.product

class ProductCommand {
    class Create(
        val name: String,
        val price: Long,
        val stock: Long,
    )

    class DecreaseStock(
        val productId: String,
        val amount: Long,
    )

    class IncreaseStock(
        val productId: String,
        val amount: Long,
    )

    class UpdateStock(
        val productId: String,
        val amount: Long,
    )
}
