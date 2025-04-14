package kr.hhplus.be.server.domain.user

/**
 * 사용자 도메인 조회 관련 클래스
 */
sealed class UserQuery {
    /**
     * ID로 사용자 조회
     */
    data class GetById(val userId: String) : UserQuery() {
        init {
            require(userId.isNotBlank()) { "userId must not be blank" }
        }
    }
    
    /**
     * 모든 사용자 조회
     */
    object GetAll : UserQuery()
} 