package kr.hhplus.be.server.infrastructure.couponevent

import jakarta.persistence.EntityManager
import kr.hhplus.be.server.domain.couponevent.CouponEvent
import kr.hhplus.be.server.domain.couponevent.CouponEventRepository
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.lock.LockKeyGenerator
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

/**
 * CouponEventRepository 인터페이스의 JPA 구현체
 */
@Repository
class CouponEventRepositoryImpl(
    private val entityManager: EntityManager,
    private val couponEventJpaRepository: CouponEventJpaRepository,
    private val couponEventRedisTemplate: RedisTemplate<String, CouponEventRedisEntity>,
) : CouponEventRepository {

    private val logger = LoggerFactory.getLogger(CouponEventRepositoryImpl::class.java)

    // 키 접두사 상수
    private val KEY_PREFIX = "orderItemRank"

    private fun generateKey(id: String): String {
        return "$KEY_PREFIX:id$id"
    }

    /**
     * 쿠폰 이벤트를 생성
     */
    override fun create(couponEvent: CouponEvent): CouponEvent {
        // Save in RDB
        val entity = CouponEventJpaEntity.fromDomain(couponEvent)
        val savedEntity = couponEventJpaRepository.save(entity)

        // Save in Redis
        val ops = couponEventRedisTemplate.opsForValue()
        val key = generateKey(couponEvent.id)
        val redisEntity = CouponEventRedisEntity.fromDomain(couponEvent)
        ops.set(key, redisEntity)
        couponEventRedisTemplate.expire(key, Duration.ofDays(365))

        // Return
        return savedEntity.toDomain()
    }

    /**
     * 주어진 id의 쿠폰 이벤트를 조회
     */
    override fun findById(id: String): CouponEvent? {
        val key = generateKey(id)
        val entity = couponEventRedisTemplate.opsForValue().get(key)
        return entity?.toDomain()
    }

    /**
     * 모든 쿠폰 이벤트를 조회
     */
    @Transactional(readOnly = true)
    override fun findAll(): List<CouponEvent> {
        return couponEventJpaRepository.findAll()
            .map { it.toDomain() }
    }

    /**
     * 주어진 쿠폰 이벤트를 저장
     */
    override fun save(couponEvent: CouponEvent): CouponEvent {
        val key = generateKey(couponEvent.id)
        val redisEntity = CouponEventRedisEntity.fromDomain(couponEvent)
        val ops = couponEventRedisTemplate.opsForValue()
        ops.set(key, redisEntity)
        return redisEntity.toDomain()
    }

    @Scheduled(fixedRate = 1000 * 60 * 60)
    @Transactional
    override fun updateRdb(){
        val ops = couponEventRedisTemplate.opsForValue()
        val keys = couponEventRedisTemplate.keys("$KEY_PREFIX:*")
        logger.info("updateRdb: keys = $keys")
        val redisEntities = ops.multiGet(keys)!!
        redisEntities
            .map { it.toDomain() }
            .map { CouponEventJpaEntity.fromDomain(it) }
            .forEach { entityManager.merge(it) }
    }

}