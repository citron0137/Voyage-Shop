package kr.hhplus.be.server.domain.coupon

interface CouponUserRepository {
    fun create(couponUser: CouponUser): CouponUser
    fun findById(couponUserId: String): CouponUser?
    fun findByUserId(userId: String): List<CouponUser>
    fun update(couponUser: CouponUser): CouponUser
} 