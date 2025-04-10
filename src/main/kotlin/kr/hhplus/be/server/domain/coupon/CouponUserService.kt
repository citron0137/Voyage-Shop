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

    /**
     * 모든 쿠폰 사용자 정보를 조회합니다.
     *
     * @return 모든 쿠폰 사용자 정보 목록
     */
    fun getAllCouponUsers(): List<CouponUser> {
        return repository.findAll()
    }

    fun use(command: CouponUserCommand.Use): CouponUser {
        val couponUser = getCouponUser(command.couponUserId)
        val usedCouponUser = couponUser.use()
        return repository.update(usedCouponUser)
    }
} 