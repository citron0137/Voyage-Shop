package kr.hhplus.be.server.controller.orderitemrank

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 주문 상품 순위 도메인 관련 Swagger 스키마 정의
 */
@Component
class OrderItemRankSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // 주문 상품 순위 응답 스키마 정의
        schemas["OrderItemRankResponse"] = createOrderItemRankResponseSchema()
        
        return schemas
    }
    
    /**
     * 주문 상품 순위 응답 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createOrderItemRankResponseSchema(): Schema<Any> {
        // 주문 상품 순위 항목 스키마
        val orderItemRankSchema = Schema<Any>()
            .type("object")
            .description("주문 상품 순위 정보")
            .addProperty("productId", 
                Schema<String>()
                    .type("string")
                    .description("상품 ID")
                    .example("550e8400-e29b-41d4-a716-446655440001"))
            .addProperty("orderCount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("주문 횟수")
                    .example(42))
        
        // 주문 상품 순위 목록 응답 스키마
        return Schema<Any>()
            .type("array")
            .items(orderItemRankSchema)
            .description("주문 상품 순위 목록")
    }
} 