package kr.hhplus.be.server.shared.lock

import java.util.concurrent.TimeUnit

/**
 * 분산 락 관리자 인터페이스
 * 다양한 분산 락 구현체(Redisson, Zookeeper 등)에 대한 추상화 제공
 */
interface DistributedLockManager {
    /**
     * LockKeyConstants와 LockKeyGenerator를 사용하여 도메인, 리소스 타입, 리소스 ID를 기반으로 락을 획득하고 작업을 실행합니다.
     *
     * @param domainPrefix 락 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceId 리소스 ID
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     * @throws LockAcquisitionException 락 획득 실패 시 발생
     */
    fun <T> executeWithDomainLock(
        domainPrefix: String,
        resourceType: String,
        resourceId: String,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T

    /**
     * LockKeyGenerator를 사용하여 도메인별 특화된 락 키로 락을 획득합니다.
     * 
     * @param domainPrefix 락 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceId 리소스 ID
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @return 락 획득 성공 여부
     */
    fun tryDomainLock(
        domainPrefix: String,
        resourceType: String,
        resourceId: String,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS
    ): Boolean

    /**
     * LockKeyGenerator를 통해 생성된 도메인별 특화된 락 키로 생성된 락을 해제합니다.
     *
     * @param domainPrefix 락 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceId 리소스 ID
     */
    fun unlockDomain(
        domainPrefix: String,
        resourceType: String,
        resourceId: String
    )
    
    /**
     * 여러 락을 안전하게 순서대로 획득합니다.
     * 데드락 방지를 위해 모든 락 획득 요청에서 동일한 락 순서를 사용해야 합니다.
     *
     * @param keys 획득할 락 키 목록 (이미 정렬된 상태여야 함)
     * @param timeouts 각 락의 타임아웃 (기본값: 10초)
     * @param timeUnit 타임아웃 시간 단위 (기본값: 초)
     * @param action 모든 락을 획득한 후 실행할 액션
     * @return 액션의 실행 결과
     */
    fun <T> withOrderedLocks(
        keys: List<String>,
        timeouts: List<Long> = List(keys.size) { 10L },
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T
    ): T
    
    /**
     * 여러 도메인과 리소스 타입, 리소스 ID에 대한 락을 안전하게 획득합니다.
     * LockKeyConstants와 LockKeyGenerator를 사용하여 각 도메인에 맞는 락 키를 생성합니다.
     *
     * @param domainResources 도메인, 리소스 타입, 리소스 ID의 삼중 값 목록
     * @param timeout 각 락의 타임아웃 (기본값: LockKeyConstants.DEFAULT_TIMEOUT)
     * @param timeUnit 타임아웃 시간 단위 (기본값: 초)
     * @param ordered 데드락 방지를 위한 락 키 정렬 여부 (기본값: true)
     * @param action 모든 락을 획득한 후 실행할 액션
     * @return 액션의 실행 결과
     */
    fun <T> withDomainLocks(
        domainResources: List<Triple<String, String, String>>,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        ordered: Boolean = true,
        action: () -> T
    ): T
    
    /**
     * 단일 도메인 내에서 여러 리소스 ID에 대한 락을 일괄적으로 획득합니다.
     * 동일한 도메인과 리소스 타입을 가진 여러 리소스 ID에 대해 락을 획득하는 편의 메소드입니다.
     *
     * @param domainPrefix 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceIds 리소스 ID 목록
     * @param timeout 락 획득 대기 시간 (기본값: LockKeyConstants.DEFAULT_TIMEOUT)
     * @param timeUnit 타임아웃 시간 단위 (기본값: 초)
     * @param action 모든 락을 획득한 후 실행할 액션
     * @return 액션의 실행 결과
     */
    fun <T> withSameDomainLocks(
        domainPrefix: String,
        resourceType: String,
        resourceIds: List<String>,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T
    ): T
    
    /**
     * 데드락 방지를 위해 여러 리소스에 대한 락 키를 일관된 순서로 정렬합니다.
     *
     * @param keys 정렬이 필요한 락 키 목록
     * @return 정렬된 락 키 목록
     */
    fun sortLockKeys(keys: List<String>): List<String>
} 