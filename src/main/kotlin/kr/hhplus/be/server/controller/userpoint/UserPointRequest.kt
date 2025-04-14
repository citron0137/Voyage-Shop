package kr.hhplus.be.server.controller.userpoint

/**
 * 사용자 포인트 요청
 */
sealed class UserPointRequest {
    /**
     * 포인트 충전 요청
     */
    data class Charge(
        val amount: Long
    ) : UserPointRequest()

    /**
     * 포인트 사용 요청
     */
    data class Use(
        val amount: Long
    ) : UserPointRequest()
} 