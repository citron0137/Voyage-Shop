package kr.hhplus.be.server.controller.shared.exception

import kr.hhplus.be.server.controller.shared.BaseResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * 일반 예외 핸들러
 * 도메인 예외가 아닌 일반적인 예외를 처리합니다.
 * 서버 오류(500)를 제외한 모든 예외는 상태 코드 200으로 응답합니다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.OK)
    fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException): BaseResponse<Nothing> {
        log.warn("잘못된 요청 파라미터: ${ex.message}")
        return BaseResponse.fail(
            "G_INVALID_PARAM", 
            "잘못된 ${ex.name} 값입니다: ${ex.value}"
        )
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.OK)
    fun handleIllegalArgument(ex: IllegalArgumentException): BaseResponse<Nothing> {
        log.warn("잘못된 인자: ${ex.message}")
        return BaseResponse.fail("G_INVALID_ARG", ex.message ?: "잘못된 요청 인자입니다")
    }
    
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(ex: RuntimeException): BaseResponse<Nothing> {
        log.error("런타임 예외 발생", ex)
        return BaseResponse.fail("G_SERVER_ERROR", "서버 오류가 발생했습니다")
    }
    
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(ex: Exception): BaseResponse<Nothing> {
        log.error("예외 발생", ex)
        return BaseResponse.fail("G_SYSTEM_ERROR", "서버 오류가 발생했습니다")
    }
} 