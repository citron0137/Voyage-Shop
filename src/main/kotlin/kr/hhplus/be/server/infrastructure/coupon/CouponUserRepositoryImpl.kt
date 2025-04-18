package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.CouponUser
import kr.hhplus.be.server.domain.coupon.CouponUserRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * CouponUserRepository 인터페이스의 JPA 구현체
 * 실제 DB와 연동하여 사용됩니다.
 */
@Repository
@Profile("!test", "!fake", "!local")
class CouponUserRepositoryImpl(private val couponUserJpaRepository: CouponUserJpaRepository) : CouponUserRepository {
    
    override fun create(couponUser: CouponUser): CouponUser {
        val couponUserEntity = CouponUserEntity.from(couponUser)
        return couponUserJpaRepository.save(couponUserEntity).toCouponUser()
    }
    
    override fun findById(couponUserId: String): CouponUser? {
        return couponUserJpaRepository.findByIdOrNull(couponUserId)?.toCouponUser()
    }
    
    override fun findByUserId(userId: String): List<CouponUser> {
        return couponUserJpaRepository.findByUserId(userId).map { it.toCouponUser() }
    }
    
    override fun update(couponUser: CouponUser): CouponUser {
        val couponUserEntity = CouponUserEntity.from(couponUser)
        return couponUserJpaRepository.save(couponUserEntity).toCouponUser()
    }
    
    override fun findAll(): List<CouponUser> {
        return couponUserJpaRepository.findAll().map { it.toCouponUser() }
    }
} 