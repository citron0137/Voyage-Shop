package kr.hhplus.be.server.infrastructure.couponevent

import kr.hhplus.be.server.domain.couponevent.CouponEvent
import kr.hhplus.be.server.domain.couponevent.CouponEventRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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
     * 모든 쿠폰 이벤트를 조회
     */
    @Transactional(readOnly = true)
    override fun findAll(): List<CouponEvent> {
        return couponEventJpaRepository.findAll()
            .map { it.toDomain() }
    }

    /**
     * 쿠폰 이벤트의 수량을 감소
     * 락을 획득한 상태에서 남은 수량을 1 감소시킴
     */
    @Transactional
    override fun decreaseStock(id: String): CouponEvent? {
        val entity = couponEventJpaRepository.findByIdWithLock(id) ?: return null
        entity.leftIssueAmount = entity.leftIssueAmount - 1
        val savedEntity = couponEventJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
} 