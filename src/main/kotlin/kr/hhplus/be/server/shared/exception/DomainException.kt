package kr.hhplus.be.server.shared.exception

/**
 * 도메인 예외를 나타내는 인터페이스
 * 모든 도메인 예외는 이 인터페이스를 구현해야 합니다.
 */
interface DomainException {
    /**
     * 예외에 대한 에러 코드
     */
    val errorCode: String
    
    /**
     * 예외 메시지
     */
    val errorMessage: String
} 