package kr.hhplus.be.server.infrastructure.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * RedisTemplate 생성을 위한 팩토리 클래스
 * 다양한 도메인에서 타입 안전한 RedisTemplate을 쉽게 만들 수 있도록 도와줍니다.
 */
object RedisTemplateFactory {
    
    /**
     * 제네릭 타입 T를 위한 RedisTemplate을 생성합니다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return 타입 T를 위한 RedisTemplate
     */
    fun <T> createRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, T> {
        val template = RedisTemplate<String, T>()
        template.connectionFactory = connectionFactory
        
        // 키는 문자열로 직렬화
        template.keySerializer = StringRedisSerializer()

        val ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("kr.hhplus.be.server.infrastructure") // 허용할 패키지 지정
            .build()

        
        // 값은 JSON으로 직렬화 (LocalDateTime 등 Java 8 시간 타입 지원)
        val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
            )

        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jsonSerializer
        
        template.afterPropertiesSet()
        
        return template
    }
} 