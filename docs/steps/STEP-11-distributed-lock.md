### **`STEP-11 - Distributed Lock`**

- 비관적 락(Pessimistic Lock)에서 분산 락(Distributed Lock)으로의 전환
- 분산 환경에서의 동시성 제어 전략 구현 및 성능 개선

> 이 단계에서는 기존 비관적 락 방식의 한계점을 파악하고 분산 락으로 전환하는 과정과 고려사항을 다룹니다.

---

## 분산 락 전환 보고서

### 1. 비관적 락에서 분산 락으로 전환하는 이유

#### 기존 비관적 락의 한계

비관적 락(Pessimistic Lock)은 다음과 같은 한계점들을 가지고 있습니다:

1. **확장성 제한**: 
   - 데이터베이스 연결 리소스를 장시간 점유하여 전체 시스템 처리량 저하
   - 트래픽 증가 시 데이터베이스 부하 집중 현상 발생

2. **단일 장애점(Single Point of Failure)**: 
   - 데이터베이스 서버 장애 시 전체 락 메커니즘 실패
   - 복구 과정에서 일관성 보장이 어려움

3. **분산 환경 지원 부족**: 
   - 여러 서버 인스턴스가 동일 데이터에 접근하는 MSA(Micro Service Architecture) 환경에 적합하지 않음
   - 서버 간 락 조정 메커니즘 부재

4. **성능 오버헤드**: 
   - 데이터베이스 수준의 락은 상대적으로 무거운 연산
   - 트랜잭션 유지 시간이 길어질수록 성능 저하 심화

5. **데드락 위험**: 
   - 복잡한 트랜잭션에서 데드락 발생 가능성 증가
   - 해결을 위한 추가 로직 필요

#### 분산 락의 장점

분산 락(Distributed Lock)은 다음과 같은 장점을 제공합니다:

1. **확장성 향상**: 
   - 데이터베이스와 분리된 락 관리로 시스템 확장성 개선
   - 트래픽 증가에도 안정적인 성능 유지

2. **고가용성**: 
   - Redis, ZooKeeper 등 분산 시스템의 고가용성 활용
   - 마스터-슬레이브 구조를 통한 장애 복구 메커니즘 제공

3. **미세 조정 가능**: 
   - 세밀한 락 단위 설정 및 타임아웃 제어 가능
   - 비즈니스 요구사항에 맞는 커스텀 락 정책 구현 용이

4. **성능 최적화**: 
   - 인메모리 기반 솔루션 사용으로 락 획득/해제 지연 최소화
   - 데이터베이스 부하 감소 및 전체 시스템 성능 향상

5. **분산 환경 지원**: 
   - 여러 서버 인스턴스 간 일관된 락 메커니즘 제공
   - 클라우드 및 컨테이너 환경에 적합한 확장 가능한 아키텍처

### 2. 분산 락 구현 시 고려사항

#### 기술 선택

분산 락을 구현하기 위한 대표적인 기술들과 각각의 특징:

| 기술 | 장점 | 단점 | 적합한 상황 |
|------|------|------|------------|
| **Redis** | • 빠른 성능<br>• 간단한 구현<br>• 널리 사용되는 익숙한 기술<br>• 다양한 클라이언트 라이브러리 | • 메모리 기반 (영구 저장 제한)<br>• 네트워크 파티션에 취약<br>• 완벽한 분산 합의 부재 | • 빠른 응답 시간 필요<br>• 대규모 동시 요청 처리<br>• 간단한 락 메커니즘 필요 |
| **ZooKeeper** | • 강력한 일관성 보장<br>• 분산 합의 알고리즘 내장<br>• 계층적 락 구조 지원<br>• 노드 상태 감시 기능 | • 복잡한 설정 및 관리<br>• 상대적으로 느린 성능<br>• 리소스 소모 큼<br>• 학습 곡선 가파름 | • 강한 일관성이 필요<br>• 복잡한 락 시나리오<br>• 노드 상태 감시 필요 |
| **etcd** | • 높은 가용성<br>• Raft 합의 알고리즘 사용<br>• 가벼운 설치 및 구성<br>• Kubernetes와 통합 용이 | • 제한된 기능<br>• 상대적으로 적은 커뮤니티<br>• Redis보다 느림 | • Kubernetes 환경<br>• 마이크로서비스 구성<br>• 설정 관리 포함 필요 |
| **Hazelcast** | • 인메모리 데이터 그리드<br>• 자바 네이티브 통합<br>• 분산 컬렉션 지원<br>• 데이터 샤딩 기능 | • 무거운 리소스 사용<br>• 설정 복잡성<br>• 학습 곡선 높음 | • 자바 기반 애플리케이션<br>• 분산 컴퓨팅 요구<br>• 복잡한 분산 데이터 구조 필요 |

현재 프로젝트에서는 다음과 같은 이유로 **Redis** 기반 분산 락을 선택했습니다:
- 간결한 구현 및 유지보수 용이성
- 높은 성능과 낮은 지연 시간
- 기존 시스템에 Redis 인프라 활용 가능
- 충분한 수준의 신뢰성 제공

#### 구현 방식

분산 락을 구현하는 몇 가지 주요 패턴:

1. **Redisson 라이브러리 사용**: 
   - Java용 Redis 클라이언트 라이브러리로 분산 락 기능 제공
   - 자동화된 락 관리 및 장애 복구 메커니즘 내장

   ```kotlin
   // Redisson 기반 분산 락 예시
   @Component
   class RedissonLockManager(private val redissonClient: RedissonClient) {
       fun <T> executeWithLock(key: String, timeout: Long, unit: TimeUnit, supplier: () -> T): T {
           val lock = redissonClient.getLock("lock:$key")
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
   }
   ```

2. **Redlock 알고리즘**: 
   - 여러 Redis 인스턴스에 동시에 락을 획득하는 방식
   - 단일 Redis 인스턴스 장애에도 안전한 락 메커니즘 제공
   - 구현 복잡성이 증가하지만 안정성 향상

3. **스핀 락(Spin Lock) 방식**: 
   - SETNX 명령어를 사용한 기본적인 분산 락 구현
   - 락 획득 실패 시 일정 시간 대기 후 재시도
   - 단순하지만 효율성이 낮은 방식

4. **어노테이션 기반 AOP 구현**: 
   - 비즈니스 로직과 락 관리 로직 분리
   - 코드 가독성 및 유지보수성 향상

### 3. 주요 위험 요소 및 대응 방안

분산 락 구현 시 고려해야 할 주요 위험 요소와 대응 방안:

#### 1. 락 획득 실패 처리

**위험**: 락 획득에 실패하는 경우 대응 전략이 없으면 사용자 경험 저하

**대응 방안**:
- 재시도 메커니즘 구현 (지수 백오프 전략)
- 대체 플로우 제공 (임시 예약 후 처리 등)
- 사용자에게 적절한 피드백 제공 (재시도 유도 또는 대안 제시)

```kotlin
fun executeWithRetry(key: String, maxRetries: Int, block: () -> T): T {
    var retries = 0
    var lastException: Exception? = null
    
    while (retries < maxRetries) {
        try {
            return lockManager.executeWithLock(key, 1, TimeUnit.SECONDS, block)
        } catch (e: LockAcquisitionException) {
            lastException = e
            retries++
            val backoffTime = (2.0.pow(retries) * 100).toLong().coerceAtMost(2000)
            Thread.sleep(backoffTime)
        }
    }
    
    throw ExhaustedException("최대 재시도 횟수 초과", lastException)
}
```

#### 2. 데드락 및 락 해제 누락

**위험**: 락을 획득한 후 예외 발생이나 서버 장애로 인해 락 해제가 누락되는 경우

**대응 방안**:
- 락에 만료 시간(TTL) 설정 (필수)
- try-finally 블록을 사용하여 락 해제 보장
- 워치독(Watchdog) 메커니즘 구현 (Redisson 자동 제공)
- 긴 작업의 경우 락 갱신 메커니즘 구현

#### 3. 네트워크 파티션 문제

**위험**: 분산 시스템에서 네트워크 파티션으로 인한 일관성 훼손

**대응 방안**:
- Redlock 알고리즘 사용 고려
- 장애 감지 및 복구 메커니즘 구현
- 낙관적 락과 같은 보조 메커니즘 결합
- 잠금 상태 모니터링 및 경고 시스템 구축

#### 4. 성능 및 확장성 문제

**위험**: 락 서버의 부하 집중 및 병목 현상 발생

**대응 방안**:
- Redis 클러스터 사용
- 락 세분화 (더 작은 단위의 리소스에 락 적용)
- 캐싱 및 배치 처리로 락 획득 횟수 감소
- 불필요한 락 제거 및 최적화

### 4. 비관적 락에서 분산 락으로 전환 과정

#### 단계별 전환 가이드

1. **현황 분석 및 계획 수립**:
   - 현재 비관적 락이 적용된 부분 식별
   - 우선 전환 대상 선정 (고부하 또는 병목 지점)
   - 분산 락 구현 방식 결정

2. **인프라 구성**:
   - Redis 서버 구성 (권장: 마스터-슬레이브 또는 클러스터)
   - 모니터링 시스템 구축
   - 장애 복구 메커니즘 준비

3. **공통 분산 락 서비스 구현**:
   - 재사용 가능한 락 관리자 구현
   - 재시도 및 예외 처리 로직 개발
   - 단위 테스트 작성

4. **점진적 적용**:
   - 중요도가 낮은 기능부터 적용 시작
   - 카나리 배포 방식으로 안정성 검증
   - 성능 및 안정성 모니터링

5. **완전 전환 및 최적화**:
   - 모든 대상 기능에 분산 락 적용 완료
   - 성능 튜닝 및 설정 최적화
   - 기존 비관적 락 코드 제거

#### 코드 마이그레이션 예시

**AS-IS (비관적 락):**

```kotlin
interface UserPointRepository {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM UserPointJpaEntity p WHERE p.userId = :userId")
    fun findByUserIdWithLock(userId: String): UserPointJpaEntity?
}

@Service
@Transactional
class UserPointService(private val userPointRepository: UserPointRepository) {
    fun charge(command: UserPointCommand.Charge): UserPoint {
        val userPoint = userPointRepository.findByUserIdWithLock(userId = command.userId)
            ?: throw UserPointException.NotFound()
        
        val chargedPoint = userPoint.charge(command.amount)
        return userPointRepository.save(chargedPoint)
    }
}
```

**TO-BE (분산 락) - 책임 분리 패턴:**

1. **전용 락 관리자 패턴:**

```kotlin
// 1. 기본 락 매니저 인터페이스
interface DistributedLockManager {
    fun <T> executeWithLock(
        key: String, 
        timeout: Long = 10, 
        unit: TimeUnit = TimeUnit.SECONDS, 
        supplier: () -> T
    ): T
}

// 2. Redis 기반 락 매니저 구현체
@Component
class RedisDistributedLockManager(private val redissonClient: RedissonClient) : DistributedLockManager {
    override fun <T> executeWithLock(key: String, timeout: Long, unit: TimeUnit, supplier: () -> T): T =
        redissonClient.getLock("lock:$key").run {
            if (!tryLock(timeout, unit)) {
                throw LockAcquisitionException("락 획득 실패: $key")
            }
            
            try {
                supplier()
            } finally {
                if (isHeldByCurrentThread) unlock()
            }
        }
}

// 3. 도메인별 전용 락 관리자
@Component
class UserLockManager(private val lockManager: DistributedLockManager) {
    fun <T> withUserPointLock(userId: String, action: () -> T): T =
        lockManager.executeWithLock(key = "user-point:$userId", supplier = action)
}

// 4. 비즈니스 로직에서 락 관리자 사용
interface UserPointRepository {
    fun findByUserId(userId: String): UserPointJpaEntity?
}

@Service
class UserPointService(
    private val userPointRepository: UserPointRepository,
    private val userLockManager: UserLockManager
) {
    @Transactional
    fun charge(command: UserPointCommand.Charge): UserPoint {
        return userLockManager.withUserPointLock(command.userId) {
            val userPoint = userPointRepository.findByUserId(userId = command.userId)
                ?: throw UserPointException.NotFound()
            
            val chargedPoint = userPoint.charge(command.amount)
            userPointRepository.save(chargedPoint)
        }
    }
}
```

2. **AOP 기반 분리 패턴:**

```kotlin
// 1. 락 관련 어노테이션 정의
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val parameterName: String = "",
    val timeout: Long = 10,
    val timeUnit: TimeUnit = TimeUnit.SECONDS
)

// 2. AOP 어스펙트 구현
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class DistributedLockAspect(private val lockManager: RedissonLockManager) {
    
    @Around("@annotation(distributedLock)")
    fun executeWithLock(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any {
        val actualKey = resolveKey(distributedLock.key, distributedLock.parameterName, joinPoint)
        return lockManager.executeWithLock(
            key = actualKey,
            timeout = distributedLock.timeout,
            unit = distributedLock.timeUnit
        ) {
            joinPoint.proceed()
        }
    }
    
    private fun resolveKey(keyPrefix: String, parameterName: String, joinPoint: ProceedingJoinPoint): String {
        if (parameterName.isBlank()) return keyPrefix
        
        val method = (joinPoint.signature as MethodSignature).method
        val parameterNameToIndex = method.parameters.mapIndexed { index, parameter -> 
            parameter.name to index 
        }.toMap()
        
        val parameterIndex = parameterNameToIndex[parameterName] 
            ?: throw IllegalArgumentException("파라미터 이름 '$parameterName'을 찾을 수 없습니다")
            
        val parameterValue = joinPoint.args[parameterIndex]
        return "$keyPrefix:$parameterValue"
    }
}

// 3. 비즈니스 로직에서 어노테이션 사용
interface UserPointRepository {
    fun findByUserId(userId: String): UserPointJpaEntity?
}

@Service
class UserPointService(private val userPointRepository: UserPointRepository) {
    
    @Transactional
    @DistributedLock(key = "user-point", parameterName = "command.userId")
    fun charge(command: UserPointCommand.Charge): UserPoint {
        val userPoint = userPointRepository.findByUserId(userId = command.userId)
            ?: throw UserPointException.NotFound()
        
        val chargedPoint = userPoint.charge(command.amount)
        return userPointRepository.save(chargedPoint)
    }
}
```

3. **파사드에서의 락 관리 패턴:**

```kotlin
// 1. 서비스 계층은 순수 비즈니스 로직에만 집중
@Service
@Transactional
class UserPointService(private val userPointRepository: UserPointRepository) {
    fun charge(userId: String, amount: Long): UserPoint {
        val userPoint = userPointRepository.findByUserId(userId)
            ?: throw UserPointException.NotFound()
        
        val chargedPoint = userPoint.charge(amount)
        return userPointRepository.save(chargedPoint)
    }
}

// 2. 파사드에서 락 관리 로직 적용
@Component
class UserPointFacade(
    private val userPointService: UserPointService,
    private val lockManager: DistributedLockManager
) {
    fun chargePoint(command: UserPointCommand.Charge): UserPoint =
        lockManager.executeWithLock(key = "user-point:${command.userId}") {
            userPointService.charge(command.userId, command.amount)
        }
}
```

각 패턴은 책임 분리와 관심사 분리라는 동일한 목적을 가지지만 서로 다른 접근 방식을 제공합니다:

1. **전용 락 관리자 패턴**은 도메인별 락 관리 로직을 캡슐화하여 코드 재사용성을 높입니다.
2. **AOP 기반 패턴**은 비즈니스 로직과 락 관리를 완전히 분리하여 선언적 방식으로 락을 적용합니다.
3. **파사드 패턴**은 서비스 계층은 순수하게 유지하면서 파사드 계층에서 트랜잭션 관리와 락 관리를 담당합니다.

이 중에서 프로젝트의 특성과 팀의 기술 스택에 가장 적합한 패턴을 선택하여 적용할 수 있습니다.

### 4. 분산 락 패턴 비교 및 선호도

각 패턴의 장단점과 적합한 상황을 비교해보면 다음과 같습니다:

#### 패턴 선호도 순위

1. **AOP 기반 분리 패턴** (가장 추천)
2. **전용 락 관리자 패턴** (차선책)
3. **파사드에서의 락 관리 패턴** (특정 상황에 적합)

#### AOP 기반 분리 패턴을 가장 선호하는 이유

**장점:**
- **완벽한 관심사 분리**: 비즈니스 로직과 락 로직이 완전히 분리되어 단일 책임 원칙을 가장 잘 준수
- **선언적 방식의 간결함**: `@DistributedLock` 어노테이션만 추가하면 되어 기존 코드 수정 최소화
- **일관성과 익숙함**: Spring의 `@Transactional`과 같은 패턴을 따르므로 개발자에게 친숙
- **유지보수성**: 락 관련 로직 변경 시 비즈니스 코드를 수정할 필요가 없음
- **확장성**: 새로운 메서드에 락 적용 시 어노테이션만 추가하면 되어 확장 용이

**단점:**
- **이해 복잡성**: AOP의 동작 원리를 이해해야 함
- **디버깅 어려움**: 프록시 기반 호출로 인해 디버깅이 어려울 수 있음
- **런타임 오류 가능성**: 어노테이션 설정 오류가 런타임에 발견됨

**적합한 상황:**
- 대규모 프로젝트, 여러 개발자가 참여하는 환경
- 일관성 있는 락 정책 적용이 중요한 경우
- Spring AOP에 익숙한 팀

#### 전용 락 관리자 패턴

**장점:**
- **도메인별 그룹화**: 관련된 락 로직을 도메인별로 모아 관리 가능
- **명시적 호출**: 락 사용이 코드에 명시적으로 드러나 의도가 분명함
- **세밀한 제어**: 복잡한 락 로직(다중 리소스 락, 조건부 락)을 구현하기 용이
- **테스트 용이성**: AOP보다 단위 테스트가 쉬움

**단점:**
- **비즈니스 로직과 혼합**: 서비스 코드에서 락 관리자를 직접 호출하므로 완전한 분리는 어려움
- **중복 코드 가능성**: 유사한 락 로직이 여러 도메인에 중복될 수 있음
- **일관성 유지 어려움**: 개발자가 락 관리자 사용을 빼먹을 수 있음

**적합한 상황:**
- 복잡한 락 로직이 필요한 경우
- AOP 적용이 어려운 환경
- 도메인별로 다른 락 정책이 필요한 경우

#### 파사드에서의 락 관리 패턴

**장점:**
- **서비스 순수성**: 도메인 서비스는 순수 비즈니스 로직만 포함하여 단순함
- **통합 지점**: 여러 서비스를 조합하는 로직과 락 관리를 한 곳에서 처리
- **비즈니스 트랜잭션 보호**: 여러 서비스 호출을 하나의 락으로 보호 가능

**단점:**
- **책임 과중**: 파사드 계층에 너무 많은 책임이 집중됨
- **락 범위 과대화**: 필요 이상으로 넓은 범위에 락이 적용될 수 있음
- **복잡성 증가**: 비즈니스 흐름이 복잡해질수록 파사드도 복잡해짐

**적합한 상황:**
- 소규모 프로젝트, MVP 단계
- 단순한 비즈니스 흐름
- 여러 서비스를 조합하는 기능에 락 적용이 필요한 경우

#### 상황별 추천 패턴

| 상황 | 추천 패턴 | 이유 |
|------|---------|------|
| **대규모 엔터프라이즈 애플리케이션** | AOP 기반 패턴 | 일관성, 관심사 분리, 확장성 |
| **복잡한 락 로직 필요** | 전용 락 관리자 패턴 | 세밀한 제어, 명확한 의도 표현 |
| **소규모 프로젝트, MVP** | 파사드 패턴 | 구현 간소화, 빠른 개발 |
| **마이크로서비스 환경** | AOP 또는 전용 락 관리자 | 서비스 독립성 유지에 적합 |
| **한시적 기능, 프로토타입** | 파사드 패턴 | 빠른 구현과 격리된 영향 범위 |

결론적으로, 대부분의 경우 AOP 기반 패턴이 최선의 선택이지만, 프로젝트의 특성과 팀의 기술적 배경에 따라 다른 패턴도 고려해볼 수 있습니다. 중요한 것은 비즈니스 로직과 락 관리 로직의 명확한 분리와 일관된 적용입니다.

### 5. 결론 및 추가 개선 방향

#### 결론

비관적 락에서 분산 락으로의 전환은 다음과 같은 명확한 이점을 제공합니다:

1. **확장성 개선**: 서버 인스턴스 증가에 따른 효율적인 성능 향상 기대
2. **응답 시간 단축**: 사용자 경험 향상 및 시스템 전반적 성능 개선 예상
3. **리소스 효율성**: 데이터베이스 연결 및 CPU 사용률 감소
4. **안정성 향상**: 데드락 발생 감소 및 락 획득 실패율 감소 기대
5. **유연한 아키텍처**: 분산 시스템 환경에 적합한 설계로 전환

#### 향후 개선 방향

1. **세밀한 락 최적화**:
   - 리소스 특성에 따른 락 전략 차별화
   - 읽기 작업과 쓰기 작업의 락 정책 분리

2. **락 모니터링 및 대시보드**:
   - 실시간 락 상태 모니터링 시스템 구축
   - 병목 현상 및 문제점 조기 발견 메커니즘

3. **다중 데이터 센터 지원**:
   - 지역적으로 분산된 환경에서의 락 정책 개선
   - 글로벌 분산 시스템 지원을 위한 전략 수립

4. **하이브리드 접근법**:
   - 특정 상황에 맞게 낙관적 락, 비관적 락, 분산 락을 조합하는 방식 연구
   - 사용 패턴에 따른 적응형 락 전략 개발 