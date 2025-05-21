package kr.hhplus.be.server.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

/**
 * Redis 설정 클래스
 * RedisTemplate 설정을 통해 Redis 연결 및 직렬화 방식을 구성합니다.
 */
@Configuration
class RedisConfig {
    
    /**
     * Redis 작업을 위한 RedisTemplate 빈을 생성합니다.
     * 
     * @param redisConnectionFactory Redis 연결 팩토리
     * @return 설정된 RedisTemplate
     */
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplateFactory.createRedisTemplate(redisConnectionFactory)
    }
} 