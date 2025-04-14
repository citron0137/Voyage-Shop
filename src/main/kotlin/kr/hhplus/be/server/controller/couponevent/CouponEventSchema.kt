package kr.hhplus.be.server.controller.couponevent

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 쿠폰 이벤트 도메인 관련 Swagger 스키마 정의
 */
@Component
class CouponEventSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // CouponEventResponse.Event 스키마 정의
        schemas["CouponEventResponse.Event"] = createCouponEventResponseSchema()
        
        // CouponEventResponse.IssueCoupon 스키마 정의
        schemas["CouponEventResponse.IssueCoupon"] = createCouponEventIssueCouponResponseSchema()
        
        // CouponEventResponse.List 스키마 정의
        schemas["CouponEventResponse.List"] = createCouponEventListResponseSchema()
        
        // CouponEventRequest.Create 스키마 정의
        schemas["CouponEventRequest.Create"] = createCouponEventCreateRequestSchema()
        
        // CouponEventRequest.IssueCoupon 스키마 정의
        schemas["CouponEventRequest.IssueCoupon"] = createCouponEventIssueCouponRequestSchema()
        
        return schemas
    }
    
    /**
     * CouponEventResponse.Event 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createCouponEventResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("쿠폰 이벤트 정보")
            .addProperty("id", 
                Schema<String>()
                    .type("string")
                    .description("쿠폰 이벤트 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
            .addProperty("benefitMethod", 
                Schema<String>()
                    .type("string")
                    .description("혜택 방식")
                    .example("DISCOUNT_PERCENTAGE"))
            .addProperty("benefitAmount", 
                Schema<String>()
                    .type("string")
                    .description("혜택 금액 또는 비율")
                    .example("10"))
            .addProperty("totalIssueAmount", 
                Schema<Number>()
                    .type("integer")
                    .format("int32")
                    .description("총 발급 수량")
                    .example(100))
            .addProperty("leftIssueAmount", 
                Schema<Number>()
                    .type("integer")
                    .format("int32")
                    .description("남은 발급 수량")
                    .example(95))
    }
    
    /**
     * CouponEventResponse.IssueCoupon 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createCouponEventIssueCouponResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("쿠폰 발급 응답")
            .addProperty("couponUserId", 
                Schema<String>()
                    .type("string")
                    .description("발급된 쿠폰 사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
    }
    
    /**
     * CouponEventResponse.List 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createCouponEventListResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("쿠폰 이벤트 목록")
            .addProperty("items", 
                Schema<Any>()
                    .type("array")
                    .items(createCouponEventResponseSchema())
                    .description("쿠폰 이벤트 목록"))
    }
    
    /**
     * CouponEventRequest.Create 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createCouponEventCreateRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("쿠폰 이벤트 생성 요청")
            .addProperty("benefitMethod", 
                Schema<String>()
                    .type("string")
                    .description("혜택 방식")
                    .example("DISCOUNT_PERCENTAGE")
                    .addEnumItem("DISCOUNT_FIXED_AMOUNT")
                    .addEnumItem("DISCOUNT_PERCENTAGE"))
            .addProperty("benefitAmount", 
                Schema<String>()
                    .type("string")
                    .description("혜택 금액 또는 비율")
                    .example("10"))
            .addProperty("totalIssueAmount", 
                Schema<Number>()
                    .type("integer")
                    .format("int32")
                    .description("총 발급 수량")
                    .example(100))
    }
    
    /**
     * CouponEventRequest.IssueCoupon 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createCouponEventIssueCouponRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("쿠폰 발급 요청")
            .addProperty("userId", 
                Schema<String>()
                    .type("string")
                    .description("쿠폰을 발급받을 사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
    }
} 