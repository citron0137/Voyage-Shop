package kr.hhplus.be.server.controller.order

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component
import java.util.ArrayList

/**
 * 주문 도메인 관련 Swagger 스키마 정의
 */
@Component
class OrderSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // 주문 응답 스키마 정의
        schemas["OrderResponse"] = createOrderResponseSchema()
        
        // 주문 생성 요청 스키마 정의
        schemas["OrderRequest"] = createOrderRequestSchema()
        
        return schemas
    }
    
    /**
     * 주문 응답 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createOrderResponseSchema(): Schema<Any> {
        // 주문 항목 응답 스키마
        val orderItemResponseSchema = Schema<Any>()
            .type("object")
            .description("주문 항목 정보")
            .addProperty("id", 
                Schema<String>()
                    .type("string")
                    .description("주문 항목 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
            .addProperty("productId", 
                Schema<String>()
                    .type("string")
                    .description("상품 ID")
                    .example("550e8400-e29b-41d4-a716-446655440001"))
            .addProperty("amount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("주문 수량")
                    .example(2))
            .addProperty("unitPrice", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("단가")
                    .example(10000))
            .addProperty("totalPrice", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("총 가격")
                    .example(20000))
        
        // 주문 할인 응답 스키마
        val orderDiscountResponseSchema = Schema<Any>()
            .type("object")
            .description("주문 할인 정보")
            .addProperty("id", 
                Schema<String>()
                    .type("string")
                    .description("주문 할인 ID")
                    .example("550e8400-e29b-41d4-a716-446655440002"))
            .addProperty("type", 
                Schema<String>()
                    .type("string")
                    .description("할인 유형")
                    .example("COUPON"))
            .addProperty("discountId", 
                Schema<String>()
                    .type("string")
                    .description("할인 ID")
                    .example("550e8400-e29b-41d4-a716-446655440003"))
            .addProperty("amount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("할인 금액")
                    .example(2000))
        
        // 주문 응답 스키마
        return Schema<Any>()
            .type("object")
            .description("주문 정보")
            .addProperty("id", 
                Schema<String>()
                    .type("string")
                    .description("주문 ID")
                    .example("550e8400-e29b-41d4-a716-446655440004"))
            .addProperty("userId", 
                Schema<String>()
                    .type("string")
                    .description("사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440005"))
            .addProperty("paymentId", 
                Schema<String>()
                    .type("string")
                    .description("결제 ID")
                    .example("550e8400-e29b-41d4-a716-446655440006"))
            .addProperty("totalAmount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("총 주문 금액")
                    .example(20000))
            .addProperty("totalDiscountAmount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("총 할인 금액")
                    .example(2000))
            .addProperty("finalAmount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("최종 결제 금액")
                    .example(18000))
            .addProperty("createdAt", 
                Schema<String>()
                    .type("string")
                    .format("date-time")
                    .description("주문 생성 시간")
                    .example("2023-01-01T12:00:00"))
            .addProperty("items", 
                Schema<Any>()
                    .type("array")
                    .items(orderItemResponseSchema)
                    .description("주문 항목 목록"))
            .addProperty("discounts", 
                Schema<Any>()
                    .type("array")
                    .items(orderDiscountResponseSchema)
                    .description("주문 할인 목록"))
    }
    
    /**
     * 주문 생성 요청 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createOrderRequestSchema(): Schema<Any> {
        // 주문 항목 요청 스키마
        val orderItemRequestSchema = Schema<Any>()
            .type("object")
            .description("주문 항목 요청")
            .addProperty("productId", 
                Schema<String>()
                    .type("string")
                    .description("상품 ID")
                    .example("550e8400-e29b-41d4-a716-446655440001"))
            .addProperty("count", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("주문 수량")
                    .example(2))
        
        // 결제 정보 요청 스키마
        val paymentRequestSchema = Schema<Any>()
            .type("object")
            .description("결제 정보")
            .addProperty("couponId", 
                Schema<String>()
                    .type("string")
                    .description("쿠폰 ID")
                    .example("550e8400-e29b-41d4-a716-446655440007"))
        
        // 주문 생성 요청 스키마
        return Schema<Any>()
            .type("object")
            .description("주문 생성 요청")
            .addProperty("userId", 
                Schema<String>()
                    .type("string")
                    .description("사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440005"))
            .addProperty("orderItemList", 
                Schema<Any>()
                    .type("array")
                    .items(orderItemRequestSchema)
                    .description("주문 항목 목록"))
            .addProperty("payment", 
                paymentRequestSchema)
    }
} 