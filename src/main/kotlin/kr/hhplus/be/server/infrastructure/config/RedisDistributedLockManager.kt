package kr.hhplus.be.server.infrastructure.redis

import kr.hhplus.be.server.shared.lock.DistributedLockManager
import kr.hhplus.be.server.shared.lock.LockAcquisitionException
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.lock.LockKeyGenerator
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Redisson 기반 분산 락 관리자 구현체
 * Redisson의 강력한 분산 락 기능을 활용하여 다양한 락 관리 기능을 제공합니다.
 */
@Component
class RedisDistributedLockManager(private val redissonClient: RedissonClient) : DistributedLockManager {
    
    private val REDIS_LOCK_PREFIX = "lock:"
    
    /**
     * Redisson의 분산 락을 사용하여 작업을 안전하게 실행합니다.
     * 락 획득에 실패하면 LockAcquisitionException을 발생시킵니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     * @throws LockAcquisitionException 락 획득 실패 시 발생
     */
    private fun <T> executeWithLock(
        key: String,
        timeout: Long,
        unit: TimeUnit,
        supplier: () -> T
    ): T {
        val lock = redissonClient.getLock("$REDIS_LOCK_PREFIX$key")
        val locked = lock.tryLock(timeout, unit)
        
        if (!locked) {
            throw LockAcquisitionException("락 획득 실패: $key")
        }
        
        try {
            return supplier()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
    
    /**
     * 지정된 키에 대한 분산 락을 획득합니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @return 락 획득 성공 여부
     */
    private fun tryLock(key: String, timeout: Long, unit: TimeUnit): Boolean {
        val lock = redissonClient.getLock("$REDIS_LOCK_PREFIX$key")
        return lock.tryLock(timeout, unit)
    }
    
    /**
     * 지정된 키에 대한 분산 락을 해제합니다.
     *
     * @param key 해제할 락의 리소스 키
     */
    private fun unlock(key: String) {
        val lock = redissonClient.getLock("$REDIS_LOCK_PREFIX$key")
        if (lock.isHeldByCurrentThread) {
            lock.unlock()
        }
    }
    
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
    override fun <T> executeWithDomainLock(
        domainPrefix: String,
        resourceType: String,
        resourceId: String,
        timeout: Long,
        unit: TimeUnit,
        supplier: () -> T
    ): T {
        // LockKeyGenerator를 사용하여 락 키 생성
        val lockKey = LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)

        // 락 획득 및 작업 실행
        return executeWithLock(lockKey, timeout, unit, supplier)
    }

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
    override fun tryDomainLock(
        domainPrefix: String,
        resourceType: String,
        resourceId: String,
        timeout: Long,
        unit: TimeUnit
    ): Boolean {
        // LockKeyGenerator를 사용하여 락 키 생성
        val lockKey = LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)
        
        // 락 획득 시도
        return tryLock(lockKey, timeout, unit)
    }

    /**
     * LockKeyGenerator를 통해 생성된 도메인별 특화된 락 키로 생성된 락을 해제합니다.
     *
     * @param domainPrefix 락 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceId 리소스 ID
     */
    override fun unlockDomain(
        domainPrefix: String,
        resourceType: String,
        resourceId: String
    ) {
        // LockKeyGenerator를 사용하여 락 키 생성
        val lockKey = LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)
        
        // 락 해제
        unlock(lockKey)
    }
    
    /**
     * 여러 락을 동시에 획득하고 작업을 실행합니다.
     * Redisson의 RedissonMultiLock 기능을 활용하여 원자적으로 여러 락을 획득합니다.
     *
     * @param keys 획득할 락 키 목록
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithMultiLock(
        keys: List<String>,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T {
        require(keys.isNotEmpty()) { "락 키 목록은 비어있을 수 없습니다." }
        
        // 단일 락인 경우 일반 락 사용
        if (keys.size == 1) {
            return executeWithLock(keys[0], timeout, unit, supplier)
        }
        
        // 여러 락인 경우 MultiLock 사용
        val locks = keys.map { redissonClient.getLock("$REDIS_LOCK_PREFIX$it") }.toTypedArray()
        val multiLock = redissonClient.getMultiLock(*locks)
        
        val locked = multiLock.tryLock(timeout, unit)
        if (!locked) {
            throw LockAcquisitionException("멀티 락 획득 실패: $keys")
        }
        
        try {
            return supplier()
        } finally {
            if (multiLock.isHeldByCurrentThread) {
                multiLock.unlock()
            }
        }
    }
    
    /**
     * 여러 도메인과 리소스 타입, 리소스 ID에 대한 락을 동시에 획득하고 작업을 실행합니다.
     * Redisson의 MultiLock 기능을 활용하여 효율적으로 여러 락을 관리합니다.
     *
     * @param domainResources 도메인, 리소스 타입, 리소스 ID의 삼중 값 목록
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithMultiDomainLock(
        domainResources: List<Triple<String, String, String>>,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T {
        require(domainResources.isNotEmpty()) { "도메인 리소스 목록은 비어있을 수 없습니다." }
        
        // 각 도메인 리소스에 대한 락 키 생성
        val lockKeys = domainResources.map { (domainPrefix, resourceType, resourceId) ->
            LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)
        }
        
        // MultiLock 사용하여 락 획득 및 작업 실행
        return executeWithMultiLock(lockKeys, timeout, unit, supplier)
    }
    
    /**
     * 읽기 락(공유 락)을 획득하고 작업을 실행합니다.
     * 여러 스레드가 동시에 읽기 락을 획득할 수 있지만, 쓰기 락이 있는 경우 대기합니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithReadLock(
        key: String,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T {
        val readWriteLock = redissonClient.getReadWriteLock("$REDIS_LOCK_PREFIX$key")
        val readLock = readWriteLock.readLock()
        
        val locked = readLock.tryLock(timeout, unit)
        if (!locked) {
            throw LockAcquisitionException("읽기 락 획득 실패: $key")
        }
        
        try {
            return supplier()
        } finally {
            if (readLock.isHeldByCurrentThread) {
                readLock.unlock()
            }
        }
    }
    
    /**
     * 쓰기 락(배타적 락)을 획득하고 작업을 실행합니다.
     * 쓰기 락은 다른 읽기 락, 쓰기 락과 배타적으로 동작합니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithWriteLock(
        key: String,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T {
        val readWriteLock = redissonClient.getReadWriteLock("$REDIS_LOCK_PREFIX$key")
        val writeLock = readWriteLock.writeLock()
        
        val locked = writeLock.tryLock(timeout, unit)
        if (!locked) {
            throw LockAcquisitionException("쓰기 락 획득 실패: $key")
        }
        
        try {
            return supplier()
        } finally {
            if (writeLock.isHeldByCurrentThread) {
                writeLock.unlock()
            }
        }
    }
    
    /**
     * 도메인별 읽기 락(공유 락)을 획득하고 작업을 실행합니다.
     *
     * @param domainPrefix 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceId 리소스 ID
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithDomainReadLock(
        domainPrefix: String,
        resourceType: String,
        resourceId: String,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T {
        val lockKey = LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)
        return executeWithReadLock(lockKey, timeout, unit, supplier)
    }
    
    /**
     * 도메인별 쓰기 락(배타적 락)을 획득하고 작업을 실행합니다.
     *
     * @param domainPrefix 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceId 리소스 ID
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithDomainWriteLock(
        domainPrefix: String,
        resourceType: String,
        resourceId: String,
        timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T {
        val lockKey = LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)
        return executeWithWriteLock(lockKey, timeout, unit, supplier)
    }
    
    /**
     * 지정된 락 키에 대해 Redisson Lock 객체를 반환합니다.
     * 고급 락 조작이 필요한 경우 사용할 수 있습니다.
     * 
     * @param key 락 키
     * @return Redisson RLock 객체
     */
    fun getLock(key: String): RLock {
        return redissonClient.getLock("$REDIS_LOCK_PREFIX$key")
    }
    
    /**
     * 지정된 도메인 정보에 대해 Redisson Lock 객체를 반환합니다.
     * 
     * @param domainPrefix 도메인 접두사 (LockKeyConstants 상수 사용)
     * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
     * @param resourceId 리소스 ID
     * @return Redisson RLock 객체
     */
    fun getDomainLock(
        domainPrefix: String,
        resourceType: String,
        resourceId: String
    ): RLock {
        val lockKey = LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)
        return getLock(lockKey)
    }

    /**
     * 데드락 방지를 위해 여러 리소스에 대한 락 키를 일관된 순서로 정렬합니다.
     *
     * @param keys 정렬이 필요한 락 키 목록
     * @return 정렬된 락 키 목록
     */
    override fun sortLockKeys(keys: List<String>): List<String> {
        return keys.sorted()
    }

    /**
     * 재귀적으로 지정된 순서대로 락을 획득합니다.
     *
     * @param keys 획득할 락 키 목록
     * @param timeouts 각 락의 타임아웃
     * @param timeUnit 타임아웃 시간 단위
     * @param index 현재 처리할 키 인덱스
     * @param action 모든 락이 획득된 후 실행할 액션
     * @return 액션의 실행 결과
     */
    private fun <T> acquireLocksRecursively(
        keys: List<String>,
        timeouts: List<Long>,
        timeUnit: TimeUnit,
        index: Int,
        action: () -> T
    ): T {
        // 모든 락을 획득했다면 액션 실행
        if (index >= keys.size) {
            return action()
        }
        
        // 현재 키로 락 획득
        return executeWithLock(keys[index], timeouts[index], timeUnit) {
            // 다음 락 획득으로 재귀 호출
            acquireLocksRecursively(keys, timeouts, timeUnit, index + 1, action)
        }
    }

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
    override fun <T> withOrderedLocks(
        keys: List<String>,
        timeouts: List<Long>,
        timeUnit: TimeUnit,
        action: () -> T
    ): T {
        require(keys.isNotEmpty()) { "락 키 목록은 비어있을 수 없습니다." }
        require(keys.size == timeouts.size) { "락 키 목록과 타임아웃 목록의 크기가 일치해야 합니다." }
        
        return acquireLocksRecursively(keys, timeouts, timeUnit, 0, action)
    }

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
    override fun <T> withDomainLocks(
        domainResources: List<Triple<String, String, String>>,
        timeout: Long,
        timeUnit: TimeUnit,
        ordered: Boolean,
        action: () -> T
    ): T {
        require(domainResources.isNotEmpty()) { "도메인 리소스 목록은 비어있을 수 없습니다." }
        
        // LockKeyGenerator를 사용하여 각 도메인 리소스에 대한 락 키 생성
        val lockKeys = domainResources.map { (domainPrefix, resourceType, resourceId) ->
            LockKeyGenerator.getDomainSpecificLockKey(domainPrefix, resourceType, resourceId)
        }
        
        // 락 키 정렬 (필요시)
        val keysToUse = if (ordered) {
            sortLockKeys(lockKeys)
        } else {
            lockKeys
        }
        
        // 모든 락에 동일한 타임아웃 적용
        val timeouts = List(keysToUse.size) { timeout }
        
        // 모든 락을 순서대로 획득하고 작업 실행
        return withOrderedLocks(
            keys = keysToUse,
            timeouts = timeouts,
            timeUnit = timeUnit,
            action = action
        )
    }

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
    override fun <T> withSameDomainLocks(
        domainPrefix: String,
        resourceType: String,
        resourceIds: List<String>,
        timeout: Long,
        timeUnit: TimeUnit,
        action: () -> T
    ): T {
        require(resourceIds.isNotEmpty()) { "리소스 ID 목록은 비어있을 수 없습니다." }
        
        // 동일한 도메인과 리소스 타입을 가진 여러 리소스 ID에 대한 삼중 값 생성
        val domainResources = resourceIds.map { resourceId ->
            Triple(domainPrefix, resourceType, resourceId)
        }
        
        // 도메인 락 획득 메소드 호출
        return withDomainLocks(
            domainResources = domainResources,
            timeout = timeout,
            timeUnit = timeUnit,
            ordered = true,
            action = action
        )
    }
} 