package kr.hhplus.be.server.infrastructure.couponevent

import kr.hhplus.be.server.infrastructure.config.RedisTemplateFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class CouponEventRedisConfig {
    @Bean
    fun couponEventRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, CouponEventRedisEntity> {
        return RedisTemplateFactory.createRedisTemplate(redisConnectionFactory)
    }
}