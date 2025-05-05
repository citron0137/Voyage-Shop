package kr.hhplus.be.server.infrastructure.orderitemrank

import kr.hhplus.be.server.domain.orderitemrank.OrderItemRank
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

/**
 * OrderItemRankRepository 인터페이스의 Redis 구현체
 * Redis의 String 자료구조를 사용하여 베스트셀러 랭킹을 관리합니다.
 */
@Repository
@Profile("!test", "!fake")
class OrderItemRankRepository(
    private val redisTemplate: RedisTemplate<String, OrderItemRankRedisEntity>
) : OrderItemRankRepository {
    
    // 키 접두사 상수
    private val KEY_PREFIX = "orderItemRank"
    
    // 캐시 만료 시간 (분)
    private val CACHE_TTL_MINUTES = 15L
    
    /**
     * 상위 랭킹 상품 컬렉션을 Redis에 저장합니다.
     *
     * @param orderItemRank 저장할 상품 랭킹 컬렉션
     * @return 저장된 상품 랭킹 컬렉션
     */
    override fun saveRank(orderItemRank: OrderItemRank): OrderItemRank {
        val key = generateKey(orderItemRank.periodInDays, orderItemRank.limit)
        val valueOperations = redisTemplate.opsForValue()
        
        // 도메인 모델을 Redis 엔티티로 변환
        val redisEntity = OrderItemRankRedisEntity.fromDomain(orderItemRank)
        
        // Redis에 저장
        valueOperations.set(key, redisEntity)
        redisTemplate.expire(key, Duration.ofMinutes(CACHE_TTL_MINUTES))
        
        return orderItemRank
    }
    
    /**
     * 상위 랭킹 상품 컬렉션을 Redis에서 조회합니다.
     *
     * @param days 최근 몇일 간의 데이터를 조회할지
     * @param limit 몇 개의 상품을 조회할지
     * @return 상위 랭킹 상품 컬렉션, 캐시에 없으면 null 반환
     */
    override fun getRank(days: Int, limit: Int): OrderItemRank? {
        val key = generateKey(days, limit)
        val valueOperations = redisTemplate.opsForValue()
        
        // Redis에서 엔티티 조회
        val redisEntity = valueOperations.get(key) ?: return null
        
        // Redis 엔티티를 도메인 모델로 변환
        return OrderItemRankRedisEntity.toDomain(redisEntity)
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
        return "$KEY_PREFIX:days$days:limit$limit"
    }
} 