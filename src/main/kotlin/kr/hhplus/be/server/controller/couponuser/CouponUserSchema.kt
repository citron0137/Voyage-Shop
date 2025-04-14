package kr.hhplus.be.server.controller.couponuser

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 쿠폰 사용자 도메인 관련 Swagger 스키마 정의
 */
@Component
class CouponUserSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // CouponUserResponse.Single 스키마 정의
        schemas["CouponUserResponse.Single"] = createCouponUserSingleResponseSchema()
        
        // CouponUserResponse.List 스키마 정의
        schemas["CouponUserResponse.List"] = createCouponUserListResponseSchema()
        
        return schemas
    }
    
    /**
     * CouponUserResponse.Single 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createCouponUserSingleResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("쿠폰 사용자 정보")
            .addProperty("id", 
                Schema<String>()
                    .type("string")
                    .description("쿠폰 사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
            .addProperty("userId", 
                Schema<String>()
                    .type("string")
                    .description("사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440001"))
            .addProperty("type", 
                Schema<String>()
                    .type("string")
                    .description("쿠폰 유형")
                    .example("DISCOUNT_FIXED_AMOUNT")
                    .addEnumItem("DISCOUNT_FIXED_AMOUNT")
                    .addEnumItem("DISCOUNT_PERCENTAGE"))
            .addProperty("discountPercentage", 
                Schema<Number>()
                    .type("integer")
                    .format("int32")
                    .description("할인 비율 (%)")
                    .example(10))
            .addProperty("fixedDiscountAmount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("고정 할인 금액")
                    .example(1000))
    }
    
    /**
     * CouponUserResponse.List 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createCouponUserListResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("쿠폰 사용자 목록")
            .addProperty("items", 
                Schema<Any>()
                    .type("array")
                    .items(createCouponUserSingleResponseSchema())
                    .description("쿠폰 사용자 목록"))
    }
} 