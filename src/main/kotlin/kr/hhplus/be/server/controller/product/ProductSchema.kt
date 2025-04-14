package kr.hhplus.be.server.controller.product

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 상품 도메인 관련 Swagger 스키마 정의
 */
@Component
class ProductSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // ProductResponse.Single 스키마 정의
        schemas["ProductResponse.Single"] = createProductSingleResponseSchema()
        
        // ProductResponse.List 스키마 정의
        schemas["ProductResponse.List"] = createProductListResponseSchema()
        
        // ProductRequest.Create 스키마 정의
        schemas["ProductRequest.Create"] = createProductCreateRequestSchema()
        
        // ProductRequest.UpdateStock 스키마 정의
        schemas["ProductRequest.UpdateStock"] = createProductUpdateStockRequestSchema()
        
        // ProductRequest.IncreaseStock 스키마 정의
        schemas["ProductRequest.IncreaseStock"] = createProductIncreaseStockRequestSchema()
        
        // ProductRequest.DecreaseStock 스키마 정의
        schemas["ProductRequest.DecreaseStock"] = createProductDecreaseStockRequestSchema()
        
        return schemas
    }
    
    /**
     * ProductResponse.Single 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createProductSingleResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("단일 상품 응답")
            .addProperty("productId", 
                Schema<String>()
                    .type("string")
                    .description("상품 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
            .addProperty("name", 
                Schema<String>()
                    .type("string")
                    .description("상품명")
                    .example("아메리카노"))
            .addProperty("price", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("상품 가격")
                    .example(4500))
            .addProperty("stock", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("상품 재고")
                    .example(100))
            .addProperty("createdAt", 
                Schema<String>()
                    .type("string")
                    .format("date-time")
                    .description("생성 시간")
                    .example("2023-01-01T00:00:00"))
            .addProperty("updatedAt", 
                Schema<String>()
                    .type("string")
                    .format("date-time")
                    .description("수정 시간")
                    .example("2023-01-01T00:00:00"))
    }
    
    /**
     * ProductResponse.List 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createProductListResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("상품 목록 응답")
            .addProperty("products", 
                Schema<Any>()
                    .type("array")
                    .items(createProductSingleResponseSchema())
                    .description("상품 목록"))
    }
    
    /**
     * ProductRequest.Create 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createProductCreateRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("상품 생성 요청")
            .addProperty("name", 
                Schema<String>()
                    .type("string")
                    .description("상품명")
                    .example("아메리카노"))
            .addProperty("price", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("상품 가격")
                    .example(4500))
            .addProperty("stock", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("초기 재고량")
                    .example(100))
    }
    
    /**
     * ProductRequest.UpdateStock 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createProductUpdateStockRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("재고 업데이트 요청")
            .addProperty("stock", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("변경할 재고량")
                    .example(50))
    }
    
    /**
     * ProductRequest.IncreaseStock 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createProductIncreaseStockRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("재고 증가 요청")
            .addProperty("amount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("증가시킬 재고량")
                    .example(20))
    }
    
    /**
     * ProductRequest.DecreaseStock 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createProductDecreaseStockRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("재고 감소 요청")
            .addProperty("amount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("감소시킬 재고량")
                    .example(10))
    }
} 