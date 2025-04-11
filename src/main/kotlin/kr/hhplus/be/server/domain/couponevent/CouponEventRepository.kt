package kr.hhplus.be.server.domain.couponevent

import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Repository

interface CouponEventRepository {
    fun save(couponEvent: CouponEvent): CouponEvent
    fun findById(id: String): Optional<CouponEvent>
    fun findAll(): List<CouponEvent>
}

@Repository
class CouponEventRepositoryImpl : CouponEventRepository {
    
    private val store = ConcurrentHashMap<String, CouponEvent>()
    
    override fun save(couponEvent: CouponEvent): CouponEvent {
        store[couponEvent.id] = couponEvent
        return couponEvent
    }
    
    override fun findById(id: String): Optional<CouponEvent> {
        return Optional.ofNullable(store[id])
    }
    
    override fun findAll(): List<CouponEvent> {
        return store.values.toList()
    }
} 