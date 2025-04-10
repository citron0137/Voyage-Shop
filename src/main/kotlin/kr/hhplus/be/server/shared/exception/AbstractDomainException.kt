package kr.hhplus.be.server.shared.exception

/**
 * 모든 도메인 예외의 기본 구현을 제공하는 추상 클래스
 */
abstract class AbstractDomainException(
    override val errorCode: String,
    override val errorMessage: String
) : RuntimeException(errorMessage), DomainException 