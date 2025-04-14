package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.couponevent.CouponEvent
import kr.hhplus.be.server.domain.couponevent.CouponEventRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 CouponEventRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeCouponEventRepository : CouponEventRepository {
    
    private val store = ConcurrentHashMap<String, CouponEvent>()
    
    override fun create(couponEvent: CouponEvent): CouponEvent {
        store[couponEvent.id] = couponEvent
        return couponEvent
    }
    
    override fun findById(id: String): CouponEvent? {
        return store[id]
    }
    
    override fun findAll(): List<CouponEvent> {
        return store.values.toList()
    }
    
    override fun decreaseStock(id: String): CouponEvent? {
        val couponEvent = store[id] ?: return null
        
        if (couponEvent.leftIssueAmount <= 0) {
            return null
        }
        
        val updatedCouponEvent = CouponEvent(
            id = couponEvent.id,
            benefitMethod = couponEvent.benefitMethod,
            benefitAmount = couponEvent.benefitAmount,
            totalIssueAmount = couponEvent.totalIssueAmount,
            leftIssueAmount = couponEvent.leftIssueAmount - 1,
            createdAt = couponEvent.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        store[id] = updatedCouponEvent
        return updatedCouponEvent
    }
} 