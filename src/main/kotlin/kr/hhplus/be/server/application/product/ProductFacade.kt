package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 상품 파사드
 * 상품 관련 비즈니스 로직을 캡슐화하고 컨트롤러에서 사용할 수 있는 단순한 인터페이스를 제공합니다.
 */
@Component
class ProductFacade(
    private val productService: ProductService
) {
    /**
     * 상품 ID로 상품 정보를 조회합니다.
     *
     * @param criteria 상품 조회 요청 기준
     * @return 상품 정보
     * @throws ProductException.ProductIdShouldNotBlank 상품 ID가 빈 값인 경우
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getProduct(criteria: ProductCriteria.GetById): ProductResult.Single {
        val product = productService.getProductById(criteria.toQuery())
        return ProductResult.Single.from(product)
    }
    
    /**
     * 모든 상품 정보를 조회합니다.
     *
     * @param criteria 상품 조회 요청 기준
     * @return 상품 목록 정보
     */
    @Transactional(readOnly = true)
    fun getAllProducts(criteria: ProductCriteria.GetAll = ProductCriteria.GetAll()): ProductResult.List {
        val products = productService.getAllProducts(criteria.toQuery())
        return ProductResult.List.from(products)
    }
    
    /**
     * 새로운 상품을 생성합니다.
     *
     * @param criteria 상품 생성 요청 기준
     * @return 생성된 상품 정보
     * @throws ProductException.NameShouldNotBlank 상품명이 빈 값인 경우
     * @throws ProductException.PriceShouldMoreThan0 가격이 0 이하인 경우
     * @throws ProductException.StockAmountShouldMoreThan0 재고가 0 미만인 경우
     * @throws ProductException.StockAmountOverflow 재고가 최대치를 초과하는 경우
     */
    @Transactional
    fun createProduct(criteria: ProductCriteria.Create): ProductResult.Single {
        val product = productService.createProduct(criteria.toCommand())
        return ProductResult.Single.from(product)
    }
    
    /**
     * 상품 재고를 업데이트합니다.
     *
     * @param criteria 상품 재고 업데이트 요청 기준
     * @return 업데이트된 상품 정보
     * @throws ProductException.ProductIdShouldNotBlank 상품 ID가 빈 값인 경우
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.StockAmountShouldMoreThan0 재고가 0 미만인 경우
     * @throws ProductException.StockAmountOverflow 재고가 최대치를 초과하는 경우
     */
    @Transactional
    fun updateStock(criteria: ProductCriteria.UpdateStock): ProductResult.Single {
        val product = productService.updateProductStock(criteria.toCommand())
        return ProductResult.Single.from(product)
    }
    
    /**
     * 상품 재고를 증가시킵니다.
     *
     * @param criteria 상품 재고 증가 요청 기준
     * @return 증가 후 상품 정보
     * @throws ProductException.ProductIdShouldNotBlank 상품 ID가 빈 값인 경우
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.IncreaseStockAmountShouldMoreThan0 증가량이 0 이하인 경우
     * @throws ProductException.StockAmountOverflow 증가 후 재고가 최대치를 초과하는 경우
     */
    @Transactional
    fun increaseStock(criteria: ProductCriteria.IncreaseStock): ProductResult.Single {
        val product = productService.increaseProductStock(criteria.toCommand())
        return ProductResult.Single.from(product)
    }
    
    /**
     * 상품 재고를 감소시킵니다.
     *
     * @param criteria 상품 재고 감소 요청 기준
     * @return 감소 후 상품 정보
     * @throws ProductException.ProductIdShouldNotBlank 상품 ID가 빈 값인 경우
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.DecreaseStockAmountShouldMoreThan0 감소량이 0 이하인 경우
     * @throws ProductException.StockAmountUnderflow 감소 후 재고가 0 미만인 경우
     */
    @Transactional
    fun decreaseStock(criteria: ProductCriteria.DecreaseStock): ProductResult.Single {
        val product = productService.decreaseProductStock(criteria.toCommand())
        return ProductResult.Single.from(product)
    }
} 