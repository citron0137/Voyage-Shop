package kr.hhplus.be.server.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import kr.hhplus.be.server.config.swagger.SchemaProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger(OpenAPI) 설정 클래스
 * DTO에 어노테이션을 추가하지 않고 별도의 스키마 정의를 통해 문서화합니다.
 */
@Configuration
class SwaggerConfig(
    private val schemaProviders: List<SchemaProvider>
) {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components()
                    .schemas(collectAllSchemas())
            )
            .info(
                Info()
                    .title("Voyage Shop API")
                    .description("Voyage Shop 서비스를 위한 API 문서")
                    .version("v1.0.0")
            )
    }
    
    /**
     * 모든 도메인의 스키마 정의를 수집합니다.
     * SchemaProvider 인터페이스를 구현한 모든 빈으로부터 스키마를 가져옵니다.
     */
    private fun collectAllSchemas(): Map<String, io.swagger.v3.oas.models.media.Schema<Any>> {
        val allSchemas = mutableMapOf<String, io.swagger.v3.oas.models.media.Schema<Any>>()
        
        // 각 도메인의 SchemaProvider로부터 스키마 수집
        schemaProviders.forEach { provider ->
            allSchemas.putAll(provider.getSchemas())
        }
        
        return allSchemas
    }
} 