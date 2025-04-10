package kr.hhplus.be.server.controller.shared.exception

import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.shared.exception.AbstractDomainException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 도메인 예외를 처리하는 핸들러
 * 모든 도메인 예외는 HTTP 상태 코드 200으로 응답하고, 응답 본문에 오류 정보를 포함합니다.
 */
@RestControllerAdvice
class DomainExceptionHandler {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @ExceptionHandler(AbstractDomainException::class)
    @ResponseStatus(HttpStatus.OK)
    fun handleDomainException(ex: AbstractDomainException): BaseResponse<Nothing> {
        // 모든 도메인 예외에 대해 로깅만 수행하고 200 OK로 응답
        log.warn("도메인 예외 발생: [${ex.errorCode}] ${ex.errorMessage}")
        
        return BaseResponse.fail(ex.errorCode, ex.errorMessage)
    }
} 