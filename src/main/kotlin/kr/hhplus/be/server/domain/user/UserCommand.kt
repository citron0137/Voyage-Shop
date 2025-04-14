package kr.hhplus.be.server.domain.user

/**
 * 사용자 도메인 명령 관련 클래스
 */
sealed class UserCommand {
    /**
     * 사용자 생성 명령
     */
    object Create : UserCommand()
} 