package kr.hhplus.be.server.controller.user.swagger

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 사용자 도메인 관련 Swagger 스키마 정의
 */
@Component
class UserSwaggerSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // UserResponseDTO 스키마 정의
        schemas["UserResponseDTO"] = createUserResponseDtoSchema()
        
        return schemas
    }
    
    /**
     * UserResponseDTO 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createUserResponseDtoSchema(): Schema<Any> {
        return Schema<Any>()
            .type("object")
            .description("사용자 응답 DTO")
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
} 