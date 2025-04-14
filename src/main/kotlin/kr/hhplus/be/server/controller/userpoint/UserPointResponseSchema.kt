package kr.hhplus.be.server.controller.userpoint

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 사용자 포인트 도메인 관련 Swagger 스키마 정의
 */
@Component
class UserPointResponseSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // UserPointResponse.Single 스키마 정의
        schemas["UserPointResponse.Single"] = createUserPointSingleResponseSchema()
        
        // UserPointResponse.List 스키마 정의
        schemas["UserPointResponse.List"] = createUserPointListResponseSchema()
        
        // UserPointRequest.Charge 스키마 정의
        schemas["UserPointRequest.Charge"] = createUserPointChargeRequestSchema()
        
        // UserPointRequest.Use 스키마 정의
        schemas["UserPointRequest.Use"] = createUserPointUseRequestSchema()
        
        return schemas
    }
    
    /**
     * UserPointResponse.Single 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createUserPointSingleResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("단일 사용자 포인트 응답")
            .addProperty("id", 
                Schema<String>()
                    .type("string")
                    .description("포인트 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
            .addProperty("userId", 
                Schema<String>()
                    .type("string")
                    .description("사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
            .addProperty("amount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("포인트 잔액")
                    .example(10000))
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
     * UserPointResponse.List 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createUserPointListResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("사용자 포인트 목록 응답")
            .addProperty("points", 
                Schema<Any>()
                    .type("array")
                    .items(createUserPointSingleResponseSchema())
                    .description("포인트 목록"))
    }
    
    /**
     * UserPointRequest.Charge 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createUserPointChargeRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("포인트 충전 요청")
            .addProperty("amount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("충전할 포인트 금액")
                    .example(10000))
    }
    
    /**
     * UserPointRequest.Use 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createUserPointUseRequestSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("포인트 사용 요청")
            .addProperty("amount", 
                Schema<Number>()
                    .type("integer")
                    .format("int64")
                    .description("사용할 포인트 금액")
                    .example(5000))
    }
} 