package kr.hhplus.be.server.infrastructure.fake

import kr.hhplus.be.server.domain.coupon.CouponUser
import kr.hhplus.be.server.domain.coupon.CouponUserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * 메모리 기반의 페이크 CouponUserRepository 구현체
 * 테스트나 개발 환경에서 사용됩니다.
 */
@Repository
@Profile("test", "fake", "local")
class FakeCouponUserRepository : CouponUserRepository {
    
    private val store = ConcurrentHashMap<String, CouponUser>()
    
    override fun create(couponUser: CouponUser): CouponUser {
        store[couponUser.couponUserId] = couponUser
        return couponUser
    }
    
    override fun findById(couponUserId: String): CouponUser? {
        return store[couponUserId]
    }
    
    override fun findByUserId(userId: String): List<CouponUser> {
        return store.values.filter { it.userId == userId }
    }
    
    override fun update(couponUser: CouponUser): CouponUser {
        store[couponUser.couponUserId] = couponUser
        return couponUser
    }
} 