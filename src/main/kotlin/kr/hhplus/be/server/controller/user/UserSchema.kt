package kr.hhplus.be.server.controller.user

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 사용자 도메인 관련 Swagger 스키마 정의
 */
@Component
class UserSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // 단일 사용자 응답 스키마 정의
        schemas["UserResponse.Single"] = createUserSingleResponseSchema()
        
        // 사용자 목록 응답 스키마 정의
        schemas["UserResponse.List"] = createUserListResponseSchema()
        
        return schemas
    }
    
    /**
     * UserResponse.Single 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createUserSingleResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("단일 사용자 응답")
            .addProperty("id", 
                Schema<String>()
                    .type("string")
                    .description("사용자 ID")
                    .example("550e8400-e29b-41d4-a716-446655440000"))
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
     * UserResponse.List 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createUserListResponseSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("사용자 목록 응답")
            .addProperty("items", 
                Schema<Any>()
                    .type("array")
                    .items(createUserSingleResponseSchema())
                    .description("사용자 목록"))
    }
} 