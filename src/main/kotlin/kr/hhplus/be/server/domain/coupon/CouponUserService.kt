package kr.hhplus.be.server.domain.coupon

import org.springframework.stereotype.Service

@Service
class CouponUserService(
    private val repository: CouponUserRepository
) {
    fun create(command: CouponUserCommand.Create): CouponUser {
        return repository.create(command.toEntity())
    }

    fun getCouponUser(couponUserId: String): CouponUser {
        return repository.findById(couponUserId)
            ?: throw CouponException.NotFound("Coupon with id: $couponUserId")
    }

    fun getCouponUsersByUserId(userId: String): List<CouponUser> {
        return repository.findByUserId(userId)
    }

    fun use(command: CouponUserCommand.Use): CouponUser {
        val couponUser = getCouponUser(command.couponUserId)
        val usedCouponUser = couponUser.use()
        return repository.update(usedCouponUser)
    }
} 