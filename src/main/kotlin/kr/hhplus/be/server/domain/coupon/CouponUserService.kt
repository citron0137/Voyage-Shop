package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class CouponUserService(
    private val repository: CouponUserRepository
) {
    /**
     * 쿠폰 생성 명령을 처리합니다.
     */
    fun create(command: CouponUserCommand.Create): CouponUser {
        return repository.create(command.toEntity())
    }

    /**
     * 쿠폰 ID로 쿠폰을 조회합니다.
     */
    fun getCouponUser(couponUserId: String): CouponUser {
        return repository.findById(couponUserId)
            ?: throw CouponException.NotFound("Coupon with id: $couponUserId")
    }

    /**
     * 유저 ID로 해당 유저의 모든 쿠폰을 조회합니다.
     * 유저 존재 여부를 확인하고 검증합니다.
     *
     * @param userId 조회할 유저 ID
     * @param userService 유저 서비스
     * @return 유저의 쿠폰 목록
     * @throws UserException.UserIdShouldNotBlank 유저 ID가 빈 값인 경우
     * @throws UserException.NotFound 유저를 찾을 수 없는 경우
     */
    fun getAllCouponsByUserId(userId: String, userService: UserService): List<CouponUser> {
        if (userId.isBlank()) {
            throw UserException.UserIdShouldNotBlank("유저 ID는 비어있을 수 없습니다.")
        }
        
        // 유저 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        return repository.findByUserId(userId)
    }

    /**
     * 쿠폰 ID로 쿠폰을 조회합니다. ID 검증을 포함합니다.
     *
     * @param couponUserId 조회할 쿠폰 ID
     * @return 쿠폰 정보
     * @throws CouponException.CouponUserIdShouldNotBlank 쿠폰 ID가 빈 값인 경우
     * @throws CouponException.NotFound 쿠폰을 찾을 수 없는 경우
     */
    fun getCouponUserWithValidation(couponUserId: String): CouponUser {
        if (couponUserId.isBlank()) {
            throw CouponException.CouponUserIdShouldNotBlank("쿠폰 ID는 비어있을 수 없습니다.")
        }
        
        return getCouponUser(couponUserId)
    }

    /**
     * 유저 ID로 해당 유저의 모든 쿠폰을 조회합니다.
     */
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

    /**
     * 쿠폰 사용 명령을 처리합니다.
     */
    fun use(command: CouponUserCommand.Use): CouponUser {
        val couponUser = getCouponUser(command.couponUserId)
        val usedCouponUser = couponUser.use()
        return repository.update(usedCouponUser)
    }

    /**
     * 사용자에게 쿠폰을 발급합니다.
     *
     * @param userId 쿠폰을 발급할 유저 ID
     * @param benefitMethod 혜택 방식 (고정 금액 할인, 퍼센트 할인)
     * @param benefitAmount 혜택 금액 (고정 금액 또는 할인율)
     * @param userService 유저 서비스
     * @return 발급된 쿠폰 정보
     * @throws UserException.UserIdShouldNotBlank 유저 ID가 빈 값인 경우
     * @throws UserException.NotFound 유저를 찾을 수 없는 경우
     * @throws CouponException.BenefitAmountShouldNotBlank 혜택 금액이 빈 값인 경우
     * @throws CouponException.BenefitAmountShouldBeNumeric 혜택 금액이 숫자가 아닌 경우
     * @throws CouponException.BenefitAmountShouldMoreThan0 혜택 금액이 0 이하인 경우
     * @throws CouponException.DiscountPercentageExceeds100 할인율이 100%를 초과하는 경우
     */
    fun issueCoupon(
        userId: String,
        benefitMethod: CouponBenefitMethod,
        benefitAmount: String,
        userService: UserService
    ): CouponUser {
        // 유저 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        val command = CouponUserCommand.Create(
            userId = userId,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount
        )
        
        return create(command)
    }

    /**
     * 쿠폰을 사용합니다.
     *
     * @param couponUserId 사용할 쿠폰 ID
     * @return 사용된 쿠폰 정보
     * @throws CouponException.CouponUserIdShouldNotBlank 쿠폰 ID가 빈 값인 경우
     * @throws CouponException.NotFound 쿠폰을 찾을 수 없는 경우
     * @throws CouponException.AlreadyUsed 이미 사용된 쿠폰인 경우
     */
    fun useCoupon(couponUserId: String): CouponUser {
        val command = CouponUserCommand.Use(couponUserId = couponUserId)
        return use(command)
    }

    /**
     * 쿠폰으로 할인 금액을 계산합니다.
     *
     * @param couponUserId 사용할 쿠폰 ID
     * @param originalAmount 원래 금액
     * @return 할인 금액
     * @throws CouponException.CouponUserIdShouldNotBlank 쿠폰 ID가 빈 값인 경우
     * @throws CouponException.NotFound 쿠폰을 찾을 수 없는 경우
     */
    fun calculateDiscountAmount(couponUserId: String, originalAmount: Long): Long {
        if (couponUserId.isBlank()) {
            throw CouponException.CouponUserIdShouldNotBlank("쿠폰 ID는 비어있을 수 없습니다.")
        }
        
        val couponUser = getCouponUser(couponUserId)
        
        return couponUser.calculateDiscountAmount(originalAmount)
    }
} 