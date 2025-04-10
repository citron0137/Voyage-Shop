package kr.hhplus.be.server.application.couponuser

import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponException
import kr.hhplus.be.server.domain.coupon.CouponUserCommand
import kr.hhplus.be.server.domain.coupon.CouponUserService
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 유저 파사드
 * 쿠폰 유저 관련 비즈니스 로직을 캡슐화하고 컨트롤러에서 사용할 수 있는 단순한 인터페이스를 제공합니다.
 */
@Component
class CouponUserFacade(
    private val couponUserService: CouponUserService,
    private val userService: UserService
) {
    /**
     * 유저 ID로 해당 유저의 모든 쿠폰을 조회합니다.
     *
     * @param userId 조회할 유저 ID
     * @return 유저의 쿠폰 목록 정보
     * @throws UserException.UserIdShouldNotBlank 유저 ID가 빈 값인 경우
     * @throws UserException.NotFound 유저를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getAllCouponsByUserId(userId: String): CouponUserListResult {
        if (userId.isBlank()) {
            throw UserException.UserIdShouldNotBlank("유저 ID는 비어있을 수 없습니다.")
        }
        
        // 유저 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        val couponUsers = couponUserService.getCouponUsersByUserId(userId)
        
        return CouponUserListResult.from(couponUsers)
    }
    
    /**
     * 쿠폰 ID로 쿠폰 정보를 조회합니다.
     *
     * @param couponUserId 조회할 쿠폰 ID
     * @return 쿠폰 정보
     * @throws CouponException.CouponUserIdShouldNotBlank 쿠폰 ID가 빈 값인 경우
     * @throws CouponException.NotFound 쿠폰을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getCouponUser(couponUserId: String): CouponUserResult {
        if (couponUserId.isBlank()) {
            throw CouponException.CouponUserIdShouldNotBlank("쿠폰 ID는 비어있을 수 없습니다.")
        }
        
        val couponUser = couponUserService.getCouponUser(couponUserId)
        
        return CouponUserResult.from(couponUser)
    }
    
    /**
     * 사용자에게 쿠폰을 발급합니다.
     *
     * @param userId 쿠폰을 발급할 유저 ID
     * @param benefitMethod 혜택 방식 (고정 금액 할인, 퍼센트 할인)
     * @param benefitAmount 혜택 금액 (고정 금액 또는 할인율)
     * @return 발급된 쿠폰 정보
     * @throws UserException.UserIdShouldNotBlank 유저 ID가 빈 값인 경우
     * @throws UserException.NotFound 유저를 찾을 수 없는 경우
     * @throws CouponException.BenefitAmountShouldNotBlank 혜택 금액이 빈 값인 경우
     * @throws CouponException.BenefitAmountShouldBeNumeric 혜택 금액이 숫자가 아닌 경우
     * @throws CouponException.BenefitAmountShouldMoreThan0 혜택 금액이 0 이하인 경우
     * @throws CouponException.DiscountPercentageExceeds100 할인율이 100%를 초과하는 경우
     */
    @Transactional
    fun issueCoupon(userId: String, benefitMethod: CouponBenefitMethod, benefitAmount: String): CouponUserResult {
        // 유저 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        val command = CouponUserCommand.Create(
            userId = userId,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount
        )
        
        val couponUser = couponUserService.create(command)
        
        return CouponUserResult.from(couponUser)
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
    @Transactional
    fun useCoupon(couponUserId: String): CouponUserResult {
        val command = CouponUserCommand.Use(couponUserId = couponUserId)
        
        val couponUser = couponUserService.use(command)
        
        return CouponUserResult.from(couponUser)
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
    @Transactional(readOnly = true)
    fun calculateDiscountAmount(couponUserId: String, originalAmount: Long): Long {
        if (couponUserId.isBlank()) {
            throw CouponException.CouponUserIdShouldNotBlank("쿠폰 ID는 비어있을 수 없습니다.")
        }
        
        val couponUser = couponUserService.getCouponUser(couponUserId)
        
        return couponUser.calculateDiscountAmount(originalAmount)
    }
} 