package kr.hhplus.be.server.infrastructure.orderitemrank

import kr.hhplus.be.server.infrastructure.config.RedisTemplateFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

/**
 * OrderItemRank에 대한 Redis 설정 클래스
 * OrderItemRank 도메인에서 필요한 RedisTemplate을 정의합니다.
 */
@Configuration
class OrderItemRankRedisConfig {
    
    /**
     * OrderItemRankRedisEntity를 위한 RedisTemplate 빈을 생성합니다.
     * 
     * @param redisConnectionFactory Redis 연결 팩토리
     * @return OrderItemRankRedisEntity 타입을 지원하는 RedisTemplate
     */
    @Bean
    fun orderItemRankRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, OrderItemRankRedisEntity> {
        return RedisTemplateFactory.createRedisTemplate(redisConnectionFactory)
    }
} 