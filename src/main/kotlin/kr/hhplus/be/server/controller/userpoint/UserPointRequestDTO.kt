package kr.hhplus.be.server.controller.userpoint.request

/**
 * 사용자 포인트 요청 DTO
 */
sealed class UserPointRequestDTO {
    /**
     * 포인트 충전 요청 DTO
     */
    data class Charge(
        val amount: Long
    ) : UserPointRequestDTO()

    /**
     * 포인트 사용 요청 DTO
     */
    data class Use(
        val amount: Long
    ) : UserPointRequestDTO()
} 