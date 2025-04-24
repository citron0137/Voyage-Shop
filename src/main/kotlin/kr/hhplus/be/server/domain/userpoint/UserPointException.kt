package kr.hhplus.be.server.domain.userpoint

import kr.hhplus.be.server.shared.exception.AbstractDomainException

sealed class UserPointException {
    class NotFound(message: String) : AbstractDomainException(
        errorCode = "USERPOINT_NOT_FOUND",
        errorMessage = "User point not found: $message"
    )
    
    class PointAmountOverflow(message: String) : AbstractDomainException(
        errorCode = "USERPOINT_AMOUNT_OVERFLOW",
        errorMessage = "Point overflow: $message"
    )
    
    class ChargeAmountShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "USERPOINT_INVALID_CHARGE",
        errorMessage = "Charge amount should more than 0: $message"
    )
    
    class PointAmountUnderflow(message: String) : AbstractDomainException(
        errorCode = "USERPOINT_AMOUNT_UNDERFLOW",
        errorMessage = "Point underflow: $message"
    )
    
    class UseAmountShouldMoreThan0(message: String) : AbstractDomainException(
        errorCode = "USERPOINT_INVALID_USE",
        errorMessage = "Use amount should more than 0: $message"
    )
    
    class UserIdShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "USERPOINT_INVALID_USER_ID",
        errorMessage = "User id should not blank: $message"
    )
}