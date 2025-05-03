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

**TO-BE (분산 락)**

현재 프로젝트에서는 다음과 같은 분산 락 패턴을 적용하고 있습니다:

1. **AOP 어노테이션 기반 분산 락**:

```kotlin
// 1. 파사드 레이어에서 @DistributedLock 어노테이션 사용
@Component
class UserPointFacade(
    private val userPointService: UserPointService,
    private val transactionHelper: TransactionHelper
) {
    @DistributedLock(
        domain = LockKeyConstants.USER_POINT_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_USER,
        resourceIdExpression = "criteria.userId"
    )
    fun chargePoint(criteria: UserPointCriteria.Charge): UserPointResult.Single {
        return transactionHelper.executeInTransaction {
            // 포인트 충전
            val userPoint = userPointService.charge(criteria.toCommand())
            UserPointResult.Single.from(userPoint)
        }
    }
}

// 2. 서비스 레이어는 순수한 비즈니스 로직만 포함
@Service
class UserPointService(private val userPointRepository: UserPointRepository) {
    // 락 없이 순수 비즈니스 로직만 포함
    fun charge(command: UserPointCommand.Charge): UserPoint {
        val userPoint = userPointRepository.findByUserId(userId = command.userId)
            ?: throw UserPointException.NotFound()
        
        val chargedPoint = userPoint.charge(command.amount)
        return userPointRepository.save(chargedPoint)
    }
}
```

2. **명시적 락 관리가 필요한 복잡한 케이스**:

```kotlin
// 여러 리소스에 락이 필요한 복잡한 시나리오
@Component
class OrderFacade(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val couponUserService: CouponUserService,
    private val lockManager: DistributedLockManager,
    private val transactionManager: PlatformTransactionManager
) {
    fun createOrder(criteria: OrderCriteria.Create): OrderResult.Single {
        // 1. 락 키 생성
        val orderKey = LockKeyGenerator.Order.userLock(criteria.userId)
        
        // 상품 락 키 생성
        val productIds = criteria.items.map { it.productId }
        val productKeys = productIds.map { LockKeyGenerator.Product.stockLock(it) }
        
        // 쿠폰 락 키 생성
        val couponKey = criteria.couponUserId?.let { LockKeyGenerator.CouponUser.idLock(it) }
        
        // 2. 락 획득 순서 정의 (사용자 -> 상품들 -> 쿠폰)
        val allKeys = listOf(orderKey) + DistributedLockUtils.sortLockKeys(productKeys) + listOfNotNull(couponKey)
        
        // 3. 타임아웃 설정
        val timeouts = listOf(LockKeyConstants.EXTENDED_TIMEOUT) + 
            List(allKeys.size - 1) { LockKeyConstants.DEFAULT_TIMEOUT }
        
        // 4. 모든 락을 순서대로 획득하고 액션 실행
        return DistributedLockUtils.withOrderedLocks(
            lockManager = lockManager,
            keys = allKeys,
            timeouts = timeouts,
            action = {
                // 모든 락을 획득한 후에 트랜잭션 시작
                val transactionTemplate = TransactionTemplate(transactionManager)
                transactionTemplate.execute {
                    // 비즈니스 로직
                    // ...
                }
            }
        )
    }
}
```

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

#### Voyage-Shop 프로젝트의 분산 락 구현

현재 Voyage-Shop 프로젝트에서는 두 가지 분산 락 패턴을 함께 사용하고 있습니다:

1. **간단한 단일 리소스 락**: `@DistributedLock` 어노테이션 사용 (AOP 기반)
2. **복잡한 다중 리소스 락**: 명시적 락 관리 코드 사용 (OrderFacade.createOrder와 같은 복잡한 시나리오)

이러한 하이브리드 접근 방식은 다음과 같은 이점을 제공합니다:
- 간단한 경우 코드 간결성 유지 (어노테이션 방식)
- 복잡한 경우 세밀한 제어 가능 (명시적 코드 방식)
- 요구사항에 맞는 유연한 락 관리 전략 적용

#### 아키텍처 계층별 락 적용 규칙

프로젝트의 일관성과 유지보수성을 위해 다음과 같은 아키텍처 계층별 락 적용 규칙을 적용합니다:

**1. 파사드 레이어 (Application Layer)**
- **락 어노테이션 사용**: 파사드 레이어에서는 `@DistributedLock` 어노테이션 사용 허용
- **트랜잭션 관리**: TransactionHelper 또는 TransactionTemplate을 명시적으로 사용
- **적용 이유**:
  - 파사드 레이어는 여러 도메인 서비스를 조합하는 책임이 있어 락의 경계를 명확히 표현할 필요가 있음
  - 선언적 락 적용으로 코드의 의도가 명확하게 드러남
  - 트랜잭션과 락의 범위를 세밀하게 제어 가능

```kotlin
@Component
class UserPointFacade(
    private val userPointService: UserPointService,
    private val transactionHelper: TransactionHelper
) {
    @DistributedLock(
        domain = LockKeyConstants.USER_POINT_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_USER,
        resourceIdExpression = "criteria.userId"
    )
    fun chargePoint(criteria: UserPointCriteria.Charge): UserPointResult.Single {
        return transactionHelper.executeInTransaction {
            // 포인트 충전
            val userPoint = userPointService.charge(criteria.toCommand())
            UserPointResult.Single.from(userPoint)
        }
    }
}
```

**2. 도메인 서비스 레이어 (Domain Layer)**
- **락 어노테이션 불허**: 도메인 서비스에서는 `@DistributedLock` 어노테이션 사용 금지
- **트랜잭션 관리**: 트랜잭션은 파사드 레이어에서 관리하거나 필요 시 명시적으로 사용
- **적용 이유**:
  - 도메인 서비스는 순수 비즈니스 로직에 집중해야 함
  - 락 관리는 애플리케이션 계층의 책임으로 분리
  - 테스트 용이성 향상 (락 의존성 없이 비즈니스 로직 테스트 가능)

```kotlin
@Service
class UserPointService(private val userPointRepository: UserPointRepository) {
    // 락 없이 순수 비즈니스 로직만 포함
    fun charge(command: UserPointCommand.Charge): UserPoint {
        val userPoint = userPointRepository.findByUserId(userId = command.userId)
            ?: throw UserPointException.NotFound()
        
        val chargedPoint = userPoint.charge(command.amount)
        return userPointRepository.save(chargedPoint)
    }
}
```

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

### 6. 락 전략 테스트 결과 및 분석

포인트 시스템의 세 가지 다른 락 전략에 대한 동시성 테스트를 수행하고 결과를 비교 분석했습니다.

#### 테스트 환경
- 최대 가상 사용자(VU): 150명 (분산 테스트 100VU, 단일 사용자 스파이크 50VU)
- 총 반복 실행: 약 7,600회
- 테스트 기간: 약 2분

#### 락 전략별 주요 결과

| 지표 | 비관적 락만 | 비관적 락 + 분산 락 | 분산 락만 |
|------|------------|-------------------|----------|
| p95 응답 시간 | 7.64ms | 10.49ms | 6.85ms |
| 평균 응답 시간 | 64.27ms | 119.48ms | 81.99ms |
| 중앙값 응답 시간 | 4.05ms | 5.16ms | 4.1ms |
| 최대 응답 시간 | 1분 31초 | 1분 31초 | 1분 31초 |
| 초당 처리량 | 62.76/초 | 62.43/초 | 62.86/초 |
| 일관성 검증 실패 | 20건 | 1건 | 2건 |
| API 성공률 | 99.96% | 99.99% | 99.96% |

#### 락 전략별 특성 분석

1. **비관적 락만 사용**
   - **장점**: 빠른 응답 시간(평균 64.27ms), 추가 인프라 불필요
   - **단점**: 동시성 이슈 발생률 높음(20건의 일관성 실패)
   - **특징**: 성능은 좋지만 데이터 일관성 보장이 약함

2. **비관적 락 + 분산 락 (이중 락)**
   - **장점**: 가장 높은 데이터 일관성(단 1건의 일관성 실패), 높은 API 성공률(99.99%)
   - **단점**: 가장 긴 응답 시간(평균 119.48ms), 구현 복잡성 증가
   - **특징**: 데이터 정확성이 중요한 금융 트랜잭션에 적합

3. **분산 락만 사용**
   - **장점**: 가장 빠른 p95 응답 시간(6.85ms), 비교적 우수한 데이터 일관성(2건 실패)
   - **단점**: Redis 등 추가 인프라 필요
   - **특징**: 성능과 일관성의 균형이 잘 맞음

#### 락 사용에도 불구하고 일관성 실패가 발생하는 원인

테스트 결과에서 주목할 점은 이중 락을 사용하더라도 일부 일관성 검증 실패가 발생한다는 것입니다. 이는 분산 시스템에서 완벽한 락 메커니즘을 구현하는 데 있어 다음과 같은 근본적인 한계가 존재하기 때문입니다:

1. **분산 환경의 본질적 한계**: 
   - 비관적 락은 단일 데이터베이스 내에서만 효과적이며, 여러 서버가 동일 데이터에 접근할 때 완벽한 보호를 제공하지 못함
   - 분산 락도 네트워크 지연이나 통신 장애에 취약함
   - CAP 정리에 따르면 분산 시스템에서 일관성, 가용성, 분할 내성을 모두 완벽히 만족하는 것은 이론적으로 불가능

2. **락 타임아웃 문제**: 
   - 락 세션이 만료되고 다른 프로세스가 접근하는 경우 데이터 불일치 발생 가능
   - 특히 최대 응답 시간이 1분 31초로 길었다는 점은 일부 요청에서 락 타임아웃이 발생했을 가능성 시사

3. **극단적 테스트 환경**: 
   - 150명의 동시 사용자가 집중적으로 요청하는 상황은 실제 환경보다 극단적인 부하 상황
   - 이러한 극한 상황에서는 락 메커니즘의 한계가 드러날 수 있음

4. **락 범위 설정 문제**: 
   - 락이 커버하는 범위가 적절하지 않거나 너무 세분화된 경우 일관성 문제 발생 가능
   - 트랜잭션 경계와 락 획득/해제 시점의 불일치가 존재할 수 있음

5. **예외 처리 부족**: 
   - 예외 발생 시 락이 제대로 해제되지 않는 경우 데드락이나 일관성 문제 발생
   - 특히 분산 락의 경우 네트워크 오류 또는 서버 장애 시 락 해제가 보장되지 않을 수 있음

이중 락 전략에서도 1건의 실패가 발생한 것은 분산 시스템의 본질적인 한계를 보여주는 사례입니다. 그러나 비관적 락만 사용(20건 실패)이나 분산 락만 사용(2건 실패)에 비해 크게 개선된 결과를 보여주었으며, 금융 트랜잭션에서 요구되는 신뢰성 수준을 충족할 수 있는 수준으로 판단됩니다.

#### 사용 사례별 권장 전략

1. **단일 서버 & 낮은 동시성 환경**: 비관적 락만 사용
2. **분산 서버 & 중간 수준 동시성**: 분산 락만 사용
3. **금융 거래 등 완벽한 일관성 필수**: 비관적 락 + 분산 락
4. **높은 확장성 & 대규모 트래픽**: 분산 락 + 락 세분화

#### 최종 권장사항

포인트 시스템과 같은 금융적 성격의 트랜잭션에서는 데이터 일관성이 가장 중요하므로, 성능 일부를 희생하더라도 이중 락 전략(비관적 락 + 분산 락)을 사용하는 것이 적절합니다. 단, 서비스 확장 시에는 API 중요도에 따라 차등 전략 적용을 고려할 수 있습니다:

- 핵심 금융 트랜잭션: 이중 락 유지
- 조회 및 통계성 작업: 락 없이 처리
- 중요도가 중간인 작업: 분산 락만 적용

이러한 접근법은 데이터 안정성을 유지하면서도 시스템 성능을 최적화할 수 있는 균형 잡힌 전략이 될 것입니다. 