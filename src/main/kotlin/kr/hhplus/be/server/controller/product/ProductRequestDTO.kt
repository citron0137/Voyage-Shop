package kr.hhplus.be.server.controller.product

/**
 * 상품 요청
 */
sealed class ProductRequest {
    /**
     * 상품 생성 요청
     */
    data class Create(
        val name: String,
        val price: Long,
        val stock: Long
    ) : ProductRequest()
    
    /**
     * 재고 업데이트 요청
     */
    data class UpdateStock(
        val stock: Long
    ) : ProductRequest()
    
    /**
     * 재고 증가 요청
     */
    data class IncreaseStock(
        val amount: Long
    ) : ProductRequest()
    
    /**
     * 재고 감소 요청
     */
    data class DecreaseStock(
        val amount: Long
    ) : ProductRequest()
} 