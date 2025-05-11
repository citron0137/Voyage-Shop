package kr.hhplus.be.server.infrastructure.orderitemrank

import kr.hhplus.be.server.domain.orderitemrank.OrderItemRank
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankRepository
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * OrderItemRankRepository 인터페이스의 Redis 구현체
 * Redis의 String 자료구조를 사용하여 베스트셀러 랭킹을 관리합니다.
 */
@Repository
@Profile("!test", "!fake")
class OrderItemRankRepository(
    private val redisTemplate: RedisTemplate<String, String>
) : OrderItemRankRepository {
    
    // 키 접두사 상수
    private val KEY_PREFIX = "orderItemRank"
    
    // 캐시 만료 시간 (분)
    private val CACHE_TTL_FROM_STARTED_AT_MAP = mapOf(
        Pair(OrderItemRankType.ONE_DAY, Duration.ofDays(7 + 1)),
        Pair(OrderItemRankType.THREE_DAY,Duration.ofDays(7 + 3)),
        Pair(OrderItemRankType.ONE_WEEK, Duration.ofDays(7 + 31))
    )
    
    private fun generateKey(type: OrderItemRankType, startedAt: LocalDate): String {
        return "$KEY_PREFIX:type$type:startedAt$startedAt"
    }

    override fun addOrderCount(
        rankType: OrderItemRankType,
        startedAt: LocalDate,
        productId: String,
        orderCount: Long
    ) {
        val key = generateKey(rankType, startedAt)
        redisTemplate.opsForZSet().incrementScore(
            key,
            productId,
            orderCount.toDouble()
        )
        val expireAt = startedAt.atStartOfDay().plus(CACHE_TTL_FROM_STARTED_AT_MAP.getOrDefault(rankType, Duration.ofDays(7)))
        val ttl = Duration.between( LocalDateTime.now(), expireAt)
        redisTemplate.expire(key, ttl)
    }

    override fun getRanks(
        rankType: OrderItemRankType,
        startedAt: LocalDate,
        limit: Long
    ): List<OrderItemRank> {
        val key = generateKey(rankType, startedAt)
        val result = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit-1) ?: return listOf()
        return result.map { OrderItemRank(rankType, startedAt, it.value!!, it.score!!.toLong() ) }
    }

    override fun deleteRanks(rankType: OrderItemRankType, startedAt: LocalDate) {
        val key = generateKey(rankType, startedAt)
        redisTemplate.delete(key)
    }

}