package kr.hhplus.be.server.config.swagger

import io.swagger.v3.oas.models.media.Schema

/**
 * Swagger 스키마 제공자 인터페이스
 * 모든 도메인별 스키마 정의 클래스는 이 인터페이스를 구현해야 합니다.
 */
interface SchemaProvider {
    /**
     * 해당 도메인의 스키마 정의를 제공합니다.
     * 
     * @return 스키마 이름을 키로 하고, 스키마 정의를 값으로 하는 맵
     */
    fun getSchemas(): Map<String, Schema<Any>>
} 