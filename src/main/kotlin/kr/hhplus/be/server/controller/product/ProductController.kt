package kr.hhplus.be.server.controller.product

import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

/**
 * 상품 컨트롤러
 */
@RestController
class ProductController(
    private val productFacade: ProductFacade
) : ProductControllerApi {
    /**
     * 상품 목록을 조회합니다.
     *
     * @return 상품 목록 정보
     */
    override fun getAllProducts(): BaseResponse<ProductResponse.List> {
        val criteria = ProductCriteria.GetAll()
        val result = productFacade.getAllProducts(criteria)
        return BaseResponse.success(ProductResponse.List.from(result))
    }
    
    /**
     * 상품을 조회합니다.
     *
     * @param productId 상품 ID
     * @return 상품 정보
     */
    override fun getProduct(@PathVariable productId: String): BaseResponse<ProductResponse.Single> {
        val criteria = ProductCriteria.GetById(productId)
        val result = productFacade.getProduct(criteria)
        return BaseResponse.success(ProductResponse.Single.from(result))
    }
    
    /**
     * 상품을 생성합니다.
     *
     * @param request 상품 생성 요청
     * @return 생성된 상품 정보
     */
    override fun createProduct(@RequestBody request: ProductRequest.Create): BaseResponse<ProductResponse.Single> {
        val criteria = ProductCriteria.Create(
            name = request.name,
            price = request.price,
            stock = request.stock
        )
        val result = productFacade.createProduct(criteria)
        return BaseResponse.success(ProductResponse.Single.from(result))
    }
    
    /**
     * 상품 재고를 업데이트합니다.
     *
     * @param productId 상품 ID
     * @param request 재고 업데이트 요청
     * @return 업데이트된 상품 정보
     */
    override fun updateStock(
        @PathVariable productId: String,
        @RequestBody request: ProductRequest.UpdateStock
    ): BaseResponse<ProductResponse.Single> {
        val criteria = ProductCriteria.UpdateStock(
            productId = productId,
            stock = request.stock
        )
        val result = productFacade.updateStock(criteria)
        return BaseResponse.success(ProductResponse.Single.from(result))
    }
    
    /**
     * 상품 재고를 증가시킵니다.
     *
     * @param productId 상품 ID
     * @param request 재고 증가 요청
     * @return 증가 후 상품 정보
     */
    override fun increaseStock(
        @PathVariable productId: String,
        @RequestBody request: ProductRequest.IncreaseStock
    ): BaseResponse<ProductResponse.Single> {
        val criteria = ProductCriteria.IncreaseStock(
            productId = productId,
            amount = request.amount
        )
        val result = productFacade.increaseStock(criteria)
        return BaseResponse.success(ProductResponse.Single.from(result))
    }
    
    /**
     * 상품 재고를 감소시킵니다.
     *
     * @param productId 상품 ID
     * @param request 재고 감소 요청
     * @return 감소 후 상품 정보
     */
    override fun decreaseStock(
        @PathVariable productId: String,
        @RequestBody request: ProductRequest.DecreaseStock
    ): BaseResponse<ProductResponse.Single> {
        val criteria = ProductCriteria.DecreaseStock(
            productId = productId,
            amount = request.amount
        )
        val result = productFacade.decreaseStock(criteria)
        return BaseResponse.success(ProductResponse.Single.from(result))
    }
}