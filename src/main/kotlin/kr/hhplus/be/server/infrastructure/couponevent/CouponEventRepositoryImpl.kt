package kr.hhplus.be.server.infrastructure.couponevent

import kr.hhplus.be.server.domain.couponevent.CouponEvent
import kr.hhplus.be.server.domain.couponevent.CouponEventRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * CouponEventRepository 인터페이스의 JPA 구현체
 */
@Repository
class CouponEventRepositoryImpl(
    private val couponEventJpaRepository: CouponEventJpaRepository
) : CouponEventRepository {

    /**
     * 쿠폰 이벤트를 생성
     */
    @Transactional
    override fun create(couponEvent: CouponEvent): CouponEvent {
        val entity = CouponEventJpaEntity.fromDomain(couponEvent)
        val savedEntity = couponEventJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * 주어진 id의 쿠폰 이벤트를 조회
     */
    @Transactional(readOnly = true)
    override fun findById(id: String): CouponEvent? {
        val entity = couponEventJpaRepository.findById(id).orElse(null)
        return entity?.toDomain()
    }

    /**
     * 주어진 id의 쿠폰 이벤트를 비관적 락과 함께 조회
     */
    @Transactional
    override fun findByIdWithLock(id: String): CouponEvent? {
        val entity = couponEventJpaRepository.findByIdWithLock(id)
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
    @Transactional
    override fun save(couponEvent: CouponEvent): CouponEvent {
        val entity = CouponEventJpaEntity.fromDomain(couponEvent)
        val savedEntity = couponEventJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
} 