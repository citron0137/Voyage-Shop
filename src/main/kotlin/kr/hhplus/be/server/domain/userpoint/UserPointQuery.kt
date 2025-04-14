package kr.hhplus.be.server.domain.userpoint

/**
 * 사용자 포인트 도메인 조회 관련 클래스
 */
sealed class UserPointQuery {
    /**
     * 사용자 ID로 포인트 조회
     */
    data class GetByUserId(val userId: String) : UserPointQuery() {
        init {
            if (userId.isBlank()) throw UserPointException.UserIdShouldNotBlank("userId는 비어있을 수 없습니다.")
        }
    }
} 