package kr.hhplus.be.server.infrastructure.orderitemrank

import kr.hhplus.be.server.domain.orderitemrank.OrderItemRank
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime

/**
 * OrderItemRankRepository 인터페이스의 Redis 구현체
 * Redis의 Sorted Set 자료구조를 사용하여 베스트셀러 랭킹을 관리합니다.
 */
@Repository
@Profile("!test", "!fake")
class RedisOrderItemRankRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) : OrderItemRankRepository {
    
    // 키 접두사 상수
    private val KEY_PREFIX = "orderItemRank"
    
    // 캐시 만료 시간 (분)
    private val CACHE_TTL_MINUTES = 15L
    
    /**
     * 상위 랭킹 상품 컬렉션을 Redis에 저장합니다.
     * Redis의 Sorted Set을 사용하여 점수(주문량)에 따라 정렬됩니다.
     *
     * @param orderItemRank 저장할 상품 랭킹 컬렉션
     * @return 저장된 상품 랭킹 컬렉션
     */
    override fun saveRank(orderItemRank: OrderItemRank): OrderItemRank {
        val key = generateKey(orderItemRank.periodInDays, orderItemRank.limit)
        val operations = redisTemplate.opsForZSet()
        
        // 기존 데이터 삭제
        redisTemplate.delete(key)
        
        // 새 데이터 저장
        orderItemRank.items.forEach { item ->
            operations.add(key, item.productId, item.orderCount.toDouble())
        }
        
        // TTL 설정
        redisTemplate.expire(key, Duration.ofMinutes(CACHE_TTL_MINUTES))
        
        return getRank(orderItemRank.periodInDays, orderItemRank.limit)
    }
    
    /**
     * 상위 랭킹 상품 컬렉션을 Redis에서 조회합니다.
     *
     * @param days 최근 몇일 간의 데이터를 조회할지
     * @param limit 몇 개의 상품을 조회할지
     * @return 상위 랭킹 상품 컬렉션, 캐시에 없으면 빈 컬렉션 반환
     */
    override fun getRank(days: Int, limit: Int): OrderItemRank {
        val key = generateKey(days, limit)
        val operations = redisTemplate.opsForZSet()
        
        // 점수(주문량) 내림차순으로 상위 limit개 항목 조회
        val typedTuples = operations.reverseRangeWithScores(key, 0, limit - 1L)
        
        if (typedTuples.isNullOrEmpty()) {
            return OrderItemRank.empty(days, limit)
        }
        
        val items = typedTuples.mapIndexed { index, tuple ->
            OrderItemRank.Item(
                productId = tuple.value as String,
                orderCount = tuple.score?.toLong() ?: 0L,
                rank = index + 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
        
        return OrderItemRank(
            items = items,
            periodInDays = days,
            limit = limit
        )
    }
    
    /**
     * 저장된 모든 랭킹 데이터를 무효화합니다.
     */
    override fun invalidateCache() {
        val pattern = "$KEY_PREFIX:*"
        val keys = redisTemplate.keys(pattern)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }
    
    /**
     * 캐시 키를 생성합니다.
     *
     * @param days 최근 몇일 간의 데이터인지
     * @param limit 몇 개의 상품인지
     * @return 캐시 키
     */
    private fun generateKey(days: Int, limit: Int): String {
        return "$KEY_PREFIX:top$limit:days$days"
    }
} 