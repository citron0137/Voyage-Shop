package kr.hhplus.be.server.domain.couponevent

import kr.hhplus.be.server.shared.exception.AbstractDomainException

/**
 * 쿠폰 이벤트 관련 예외의 기본 클래스
 */
abstract class CEException(
    errorCode: String,
    errorMessage: String
) : AbstractDomainException(errorCode, errorMessage)

/**
 * 쿠폰 이벤트를 찾을 수 없을 때 발생하는 예외
 */
class CENotFoundException(
    val couponEventId: String
) : CEException(
    errorCode = "CE_NOT_FOUND",
    errorMessage = "Coupon event not found: $couponEventId"
)

/**
 * 쿠폰 재고가 없을 때 발생하는 예외
 */
class CEStockEmptyException(
    val couponEventId: String
) : CEException(
    errorCode = "CE_STOCK_EMPTY",
    errorMessage = "Coupon stock is empty for event: $couponEventId"
)

/**
 * 쿠폰 재고가 없을 때 발생하는 예외 (리팩토링 버전)
 */
class CEOutOfStockException(
    message: String
) : CEException(
    errorCode = "CE_OUT_OF_STOCK",
    errorMessage = message
)

/**
 * 잘못된 혜택 방식 값이 입력되었을 때 발생하는 예외
 */
class CEInvalidBenefitMethodException(
    val method: String
) : CEException(
    errorCode = "CE_INVALID_BENEFIT_METHOD",
    errorMessage = "Invalid benefit method: $method"
) 