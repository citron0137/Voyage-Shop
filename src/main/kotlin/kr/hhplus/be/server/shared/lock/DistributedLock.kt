package kr.hhplus.be.server.shared.lock

import java.util.concurrent.TimeUnit

/**
 * 분산 락 어노테이션
 * 메서드에 적용하여 해당 메서드 실행 시 분산 락을 획득하도록 합니다.
 *
 * @param key 락의 키 접두사 (실제 키는 접두사 + 파라미터 값으로 구성)
 * @param parameterName 락 키 생성에 사용할 파라미터 이름
 * @param timeout 락 획득 대기 시간
 * @param timeUnit 시간 단위
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val parameterName: String = "",
    val timeout: Long = 10,
    val timeUnit: TimeUnit = TimeUnit.SECONDS
) 