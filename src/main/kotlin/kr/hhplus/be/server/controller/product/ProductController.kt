package kr.hhplus.be.server.controller.product

import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.controller.product.request.ProductRequestDTO
import kr.hhplus.be.server.controller.product.response.ProductResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 상품 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productFacade: ProductFacade
) {
    /**
     * 상품 목록을 조회합니다.
     *
     * @return 상품 목록 정보
     */
    @GetMapping
    fun getAllProducts(): ResponseEntity<ProductResponseDTO.List> {
        val result = productFacade.getAllProducts()
        return ResponseEntity.ok(ProductResponseDTO.List.from(result))
    }
    
    /**
     * 상품을 조회합니다.
     *
     * @param productId 상품 ID
     * @return 상품 정보
     */
    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: String): ResponseEntity<ProductResponseDTO.Single> {
        val result = productFacade.getProduct(productId)
        return ResponseEntity.ok(ProductResponseDTO.Single.from(result))
    }
    
    /**
     * 상품을 생성합니다.
     *
     * @param request 상품 생성 요청 DTO
     * @return 생성된 상품 정보
     */
    @PostMapping
    fun createProduct(@RequestBody request: ProductRequestDTO.Create): ResponseEntity<ProductResponseDTO.Single> {
        val result = productFacade.createProduct(
            name = request.name,
            price = request.price,
            stock = request.stock
        )
        return ResponseEntity.ok(ProductResponseDTO.Single.from(result))
    }
    
    /**
     * 상품 재고를 업데이트합니다.
     *
     * @param productId 상품 ID
     * @param request 재고 업데이트 요청 DTO
     * @return 업데이트된 상품 정보
     */
    @PutMapping("/{productId}/stock")
    fun updateStock(
        @PathVariable productId: String,
        @RequestBody request: ProductRequestDTO.UpdateStock
    ): ResponseEntity<ProductResponseDTO.Single> {
        val result = productFacade.updateStock(productId, request.stock)
        return ResponseEntity.ok(ProductResponseDTO.Single.from(result))
    }
    
    /**
     * 상품 재고를 증가시킵니다.
     *
     * @param productId 상품 ID
     * @param request 재고 증가 요청 DTO
     * @return 증가 후 상품 정보
     */
    @PostMapping("/{productId}/stock/increase")
    fun increaseStock(
        @PathVariable productId: String,
        @RequestBody request: ProductRequestDTO.IncreaseStock
    ): ResponseEntity<ProductResponseDTO.Single> {
        val result = productFacade.increaseStock(productId, request.amount)
        return ResponseEntity.ok(ProductResponseDTO.Single.from(result))
    }
    
    /**
     * 상품 재고를 감소시킵니다.
     *
     * @param productId 상품 ID
     * @param request 재고 감소 요청 DTO
     * @return 감소 후 상품 정보
     */
    @PostMapping("/{productId}/stock/decrease")
    fun decreaseStock(
        @PathVariable productId: String,
        @RequestBody request: ProductRequestDTO.DecreaseStock
    ): ResponseEntity<ProductResponseDTO.Single> {
        val result = productFacade.decreaseStock(productId, request.amount)
        return ResponseEntity.ok(ProductResponseDTO.Single.from(result))
    }
}