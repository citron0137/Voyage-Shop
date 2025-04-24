package kr.hhplus.be.server.domain.couponuser

import org.springframework.stereotype.Service

@Service
class CouponUserService(
    private val repository: CouponUserRepository
) {
    /**
     * 사용자에게 쿠폰을 발급합니다.
     *
     * @param command 쿠폰 발급 명령
     * @return 발급된 쿠폰 정보
     * @throws CouponUserExcpetion.UserIdShouldNotBlank 유저 ID가 빈 값인 경우
     * @throws CouponUserExcpetion.BenefitAmountShouldNotBlank 혜택 금액이 빈 값인 경우
     * @throws CouponUserExcpetion.BenefitAmountShouldBeNumeric 혜택 금액이 숫자가 아닌 경우
     * @throws CouponUserExcpetion.BenefitAmountShouldMoreThan0 혜택 금액이 0 이하인 경우
     * @throws CouponUserExcpetion.DiscountPercentageExceeds100 할인율이 100%를 초과하는 경우
     */
    fun create(command: CouponUserCommand.Create): CouponUser {
        return repository.create(command.toEntity())
    }

    /**
     * 쿠폰 ID로 쿠폰을 조회합니다.
     *
     * @param command 쿠폰 ID로 조회 명령
     * @return 쿠폰 정보
     * @throws CouponUserExcpetion.CouponUserIdShouldNotBlank 쿠폰 ID가 빈 값인 경우
     * @throws CouponUserExcpetion.NotFound 쿠폰을 찾을 수 없는 경우
     */
    fun getCouponUser(command: CouponUserCommand.GetById): CouponUser {
        return repository.findById(command.couponUserId)
            ?: throw CouponUserException.NotFound("Coupon with id: ${command.couponUserId}")
    }

    /**
     * 유저 ID로 해당 유저의 모든 쿠폰을 조회합니다.
     *
     * @param command 유저 ID로 쿠폰 조회 명령
     * @return 유저의 쿠폰 목록
     * @throws CouponUserExcpetion.UserIdShouldNotBlank 유저 ID가 빈 값인 경우
     */
    fun getAllCouponsByUserId(command: CouponUserCommand.GetByUserId): List<CouponUser> {
        return repository.findByUserId(command.userId)
    }

    /**
     * 모든 쿠폰 사용자 정보를 조회합니다.
     *
     * @param command 모든 쿠폰 조회 명령
     * @return 모든 쿠폰 사용자 정보 목록
     */
    fun getAllCouponUsers(command: CouponUserCommand.GetAll): List<CouponUser> {
        return repository.findAll()
    }

    /**
     * 쿠폰을 사용합니다.
     *
     * @param command 쿠폰 사용 명령
     * @return 사용된 쿠폰 정보
     * @throws CouponUserExcpetion.CouponUserIdShouldNotBlank 쿠폰 ID가 빈 값인 경우
     * @throws CouponUserExcpetion.NotFound 쿠폰을 찾을 수 없는 경우
     * @throws CouponUserExcpetion.AlreadyUsed 이미 사용된 쿠폰인 경우
     */
    fun useCoupon(command: CouponUserCommand.Use): CouponUser {
        val couponUser = repository.findByIdWithLock(command.couponUserId)
            ?: throw CouponUserException.NotFound("Coupon with id: ${command.couponUserId}")
            
        val usedCouponUser = couponUser.use()
        return repository.update(usedCouponUser)
    }

    /**
     * 쿠폰으로 할인 금액을 계산합니다.
     *
     * @param command 쿠폰 할인 계산 명령
     * @return 할인 금액
     * @throws CouponUserExcpetion.CouponUserIdShouldNotBlank 쿠폰 ID가 빈 값인 경우
     * @throws CouponUserExcpetion.NotFound 쿠폰을 찾을 수 없는 경우
     * @throws CouponUserExcpetion.InvalidOriginalAmount 유효하지 않은 원래 금액인 경우
     */
    fun calculateDiscountAmount(command: CouponUserCommand.CalculateDiscount): Long {
        val couponUser = repository.findById(command.couponUserId)
            ?: throw CouponUserException.NotFound("Coupon with id: ${command.couponUserId}")
        
        return couponUser.calculateDiscountAmount(command.originalAmount)
    }
} 