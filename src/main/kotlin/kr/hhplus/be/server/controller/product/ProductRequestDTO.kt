package kr.hhplus.be.server.controller.product.request

/**
 * 상품 요청 DTO
 */
sealed class ProductRequestDTO {
    /**
     * 상품 생성 요청 DTO
     */
    data class Create(
        val name: String,
        val price: Long,
        val stock: Long
    ) : ProductRequestDTO()
    
    /**
     * 재고 업데이트 요청 DTO
     */
    data class UpdateStock(
        val stock: Long
    ) : ProductRequestDTO()
    
    /**
     * 재고 증가 요청 DTO
     */
    data class IncreaseStock(
        val amount: Long
    ) : ProductRequestDTO()
    
    /**
     * 재고 감소 요청 DTO
     */
    data class DecreaseStock(
        val amount: Long
    ) : ProductRequestDTO()
} 