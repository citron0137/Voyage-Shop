package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.shared.exception.AbstractDomainException

sealed class UserException {
    class NotFound(message: String) : AbstractDomainException(
        errorCode = "USER_NOT_FOUND",
        errorMessage = "user not found: $message"
    )
    
    class UserIdShouldNotBlank(message: String) : AbstractDomainException(
        errorCode = "USER_INVALID_ID",
        errorMessage = "User id should not blank: $message"
    )
} 