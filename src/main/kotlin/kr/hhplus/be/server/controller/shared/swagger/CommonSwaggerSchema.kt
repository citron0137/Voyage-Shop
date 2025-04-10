package kr.hhplus.be.server.controller.shared.swagger

import io.swagger.v3.oas.models.media.Schema
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.stereotype.Component

/**
 * 공통 Swagger 스키마 정의
 * 모든 API에서 공통으로 사용하는 응답 형식 등을 정의합니다.
 */
@Component
class CommonSwaggerSchema : SchemaProvider {

    override fun getSchemas(): Map<String, Schema<Any>> {
        val schemas = mutableMapOf<String, Schema<Any>>()
        
        // BaseResponse 스키마 정의
        schemas["BaseResponse"] = createBaseResponseSchema()
        
        return schemas
    }
    
    /**
     * BaseResponse 스키마를 정의합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createBaseResponseSchema(): Schema<Any> {
        val errorSchema = Schema<Any>()
            .type("object")
            .description("오류 정보")
            .addProperty("code", 
                Schema<String>()
                    .type("string")
                    .description("오류 코드")
                    .example("U_NOT_FOUND"))
            .addProperty("message", 
                Schema<String>()
                    .type("string")
                    .description("오류 메시지")
                    .example("user not found: 123"))
        
        return Schema<Any>()
            .type("object")
            .description("API 응답 공통 형식")
            .addProperty("success", 
                Schema<Boolean>()
                    .type("boolean")
                    .description("성공 여부")
                    .example(true))
            .addProperty("data", 
                Schema<Any>()
                    .description("응답 데이터 (성공시에만 데이터 포함)"))
            .addProperty("error", errorSchema)
    }
} 