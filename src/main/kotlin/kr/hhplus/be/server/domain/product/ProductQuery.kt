package kr.hhplus.be.server.domain.product

/**
 * 상품 도메인 조회 관련 클래스
 */
sealed class ProductQuery {
    /**
     * ID로 상품 조회
     */
    data class GetById(val productId: String) : ProductQuery() {
        init {
            if (productId.isBlank()) throw ProductException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
        }
    }
    
    /**
     * 모든 상품 조회
     */
    object GetAll : ProductQuery()
} 