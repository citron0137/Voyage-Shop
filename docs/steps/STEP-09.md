### **`STEP09 - Concurrency`**

- 본인의 애플리케이션 내에서 발생할 수 있는 동시성 이슈를 식별
- 동시성 이슈를 해결하기 위해 적합한 DB Lock 적용 및 AS-IS / TO-BE 비교 및 보고서 작성

> 보고서는 `문제 식별` - `분석` - `해결` - `대안` 의 항목들을 기재해 주시기 바랍니다.

---

## 동시성 이슈 해결 보고서

### 1. 문제 식별

본 쇼핑몰 애플리케이션에서는 다음과 같은 동시성 이슈가 식별되었습니다:

- **사용자 포인트 관리**: 동시에 여러 트랜잭션에서 같은 사용자의 포인트를 조회하고 업데이트할 때 경쟁 상태(Race Condition)가 발생할 수 있습니다.
- **상품 재고 관리**: 동시에 여러 사용자가 같은 상품을 구매할 때 재고 수량이 정확하게 감소하지 않을 수 있습니다.
- **쿠폰 재고 관리**: 동시에 여러 사용자가 제한된 수량의 쿠폰을 발급받을 때 쿠폰이 초과 발급될 수 있습니다.

이러한 경쟁 상태는 다음과 같은 문제를 초래할 수 있습니다:
- 데이터 일관성 손상
- 금전적 손실(포인트 누락)
- 사용자 경험 저하
- 비즈니스 로직 오작동

### 2. 분석

#### AS-IS: 비관적 락 적용 전

기존 코드는 다음과 같은 구조로 작동했습니다:

```kotlin
@Transactional
fun charge(command: UserPointCommand.Charge): UserPoint {
    val userPoint = userPointRepository.findByUserId(userId = command.userId)
        ?: throw UserPointException.NotFound("userId(${command.userId})로 UserPoint를 찾을 수 없습니다.")
        
    if (userPoint.amount > MAX_USER_POINT - command.amount) {
        throw UserPointException.PointAmountOverflow("충전 가능 최대치를 초과했습니다.")
    }
    
    val chargedPoint = userPoint.charge(command.amount)
    return userPointRepository.save(chargedPoint)
}
```

이 구현에서는 다음과 같은 문제점이 있었습니다:

1. **Lost Update 문제**: 두 개 이상의 트랜잭션이 같은 데이터를 동시에 조회하고 각각 업데이트할 경우, 한 트랜잭션의 변경사항이 다른 트랜잭션에 의해 덮어쓰여질 수 있습니다.
2. **Phantom Read**: 한 트랜잭션이 조회한 후 다른 트랜잭션에 의해 데이터가 변경됨으로써 일관성이 깨질 수 있습니다.

동시성 테스트에서 이런 문제가 확인되었습니다:
```kotlin
@Test
@DisplayName("동시에 사용자 포인트를 충전해도 정확한 잔액이 유지된다")
fun chargePointConcurrencyTest() {
    // 100개의 스레드에서 동시에 포인트 충전 시도
    // 결과: 예상 포인트와 실제 포인트 금액이 불일치
}
```

### 3. 해결

#### TO-BE: 비관적 락 적용 후

동시성 문제를 해결하기 위해 비관적 락(Pessimistic Lock)을 적용했습니다:

1. JPA 리포지토리에 락 기능 추가:
```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM UserPointJpaEntity p WHERE p.userId = :userId")
fun findByUserIdWithLock(userId: String): UserPointJpaEntity?
```

2. 서비스 계층에서 락이 적용된 메서드 사용:
```kotlin
@Transactional
fun charge(command: UserPointCommand.Charge): UserPoint {
    val userPoint = userPointRepository.findByUserIdWithLock(userId = command.userId)
        ?: throw UserPointException.NotFound("userId(${command.userId})로 UserPoint를 찾을 수 없습니다.")
    
    // 이후 코드...
}
```

이 구현은 다음과 같은 이점을 제공합니다:

1. **동시성 제어**: 한 트랜잭션이 레코드를 수정하는 동안 다른 트랜잭션은 해당 레코드에 접근할 수 없게 됩니다.
2. **데이터 일관성 보장**: 트랜잭션이 완료될 때까지 다른 트랜잭션에서는 변경된 데이터를 볼 수 없습니다.
3. **Race Condition 방지**: 여러 트랜잭션이 동시에 같은 데이터를 수정하려고 시도해도 순차적으로 처리됩니다.

동일한 동시성 테스트를 비관적 락 적용 후 실행했을 때:
```
Total Attempts: 100
Successful Charges: 100
Optimistic Lock Failures: 0
Expected Final Balance: 1000
Actual Final Balance: 1000
```

모든 트랜잭션이 성공적으로 처리되었으며, 예상된 포인트 금액과 실제 금액이 일치했습니다.

### 4. 대안

비관적 락 외에도 다음과 같은 대안이 고려될 수 있습니다:

1. **낙관적 락(Optimistic Locking)**:
   - 버전 필드를 사용하여 충돌 감지
   - 장점: 데이터베이스 리소스 사용이 적음, 높은 동시성
   - 단점: 충돌 시 재시도 로직 필요, 높은 충돌 가능성이 있는 환경에서는 성능 저하

2. **분산 락(Distributed Lock)**:
   - Redis나 ZooKeeper 등을 사용한 외부 락 관리
   - 장점: 분산 환경에서 효과적, 더 세밀한 락 제어 가능
   - 단점: 추가 인프라 필요, 구현 복잡성 증가

3. **트랜잭션 직렬화(Serializable Isolation Level)**:
   - 데이터베이스 트랜잭션 격리 수준을 가장 엄격한 수준으로 설정
   - 장점: 프로그래밍 모델 단순화, 완전한 직렬화 보장
   - 단점: 성능 저하, 교착 상태 위험 증가

#### 동시성 제어 전략 비교표

| 전략 | 장점 | 단점 | 적합한 상황 |
|------|------|------|------------|
| **비관적 락<br>(현재 구현)** | • 강력한 데이터 일관성 보장<br>• 재시도 로직 불필요<br>• 충돌이 많은 환경에서 효율적<br>• 트랜잭션 실패율 낮음 | • 데이터베이스 리소스 사용량 많음<br>• 확장성 제한<br>• 데드락 위험<br>• 대기 시간 증가 가능 | • 변경이 빈번한 리소스<br>• 금융 데이터 처리<br>• 재고 관리<br>• 충돌 발생 확률이 높은 환경 |
| **낙관적 락** | • 리소스 사용량 적음<br>• 높은 동시성 지원<br>• 데드락 위험 없음<br>• 락 획득을 위한 대기 없음 | • 충돌 시 재시도 로직 필요<br>• 충돌 빈도가 높을 경우 성능 저하<br>• 구현 복잡성 증가<br>• 충돌 해결 오버헤드 | • 읽기 작업이 많은 환경<br>• 충돌 가능성이 낮은 환경<br>• 짧은 트랜잭션<br>• 사용자 프로필 정보 관리 |
| **분산 락** | • 분산 시스템 지원<br>• 세밀한 락 제어 가능<br>• 확장성 좋음<br>• 외부 조정자를 통한 교착 상태 방지 | • 추가 인프라 필요<br>• 구현 및 유지보수 복잡성<br>• 네트워크 지연 발생 가능<br>• 외부 의존성 증가 | • 마이크로서비스 아키텍처<br>• 클러스터 환경<br>• 분산 스케줄링<br>• 세션 관리 |
| **트랜잭션 직렬화** | • 프로그래밍 모델 단순화<br>• 완전한 데이터 일관성 보장<br>• 개발자 편의성<br>• 모든 동시성 이슈 방지 | • 심각한 성능 저하<br>• 확장성 제한<br>• 교착 상태 위험 증가<br>• 동시 트랜잭션 처리량 감소 | • 매우 중요한 금융 거래<br>• 데이터 정확성이 최우선인 경우<br>• 규제 준수가 필요한 작업<br>• 트래픽이 적은 시스템 |

현재 구현된 비관적 락은 다음과 같은 이유로 최적의 선택이었습니다:
- 충돌 빈도가 높은 환경(예: 이벤트 쿠폰 발급)에 적합
- 재시도 로직 없이 일관성 보장
- 기존 JPA 인프라를 활용한 간단한 구현
- 포인트와 재고 같은 중요 자원의 정확성 보장

하지만 성능 요구사항이 변경되거나 시스템 규모가 확장됨에 따라 다른 전략으로의 전환도 고려할 수 있습니다.

### (추가) 데드락 문제와 해결 전략

주문 처리 과정에서 여러 상품의 재고를 동시에 업데이트할 때 데드락(Deadlock) 문제가 발생할 수 있습니다. 이는 비관적 락을 적용할 때 특히 주의해야 할 사항입니다.

#### 데드락 시나리오

다음과 같은 상황에서 데드락이 발생할 수 있습니다:

1. 고객 A가 상품 X와 Y를 주문합니다 (트랜잭션 A)
2. 고객 B가 상품 Y와 X를 주문합니다 (트랜잭션 B)
3. 실행 순서:
   - 트랜잭션 A가 상품 X의 락을 획득합니다
   - 트랜잭션 B가 상품 Y의 락을 획득합니다
   - 트랜잭션 A가 상품 Y의 락을 획득하려고 하지만, B가 이미 보유 중이므로 대기합니다
   - 트랜잭션 B가 상품 X의 락을 획득하려고 하지만, A가 이미 보유 중이므로 대기합니다
   - 결과: 두 트랜잭션이 서로가 보유한 락이 해제되기를 무한정 기다리는 교착 상태 발생

#### 데드락 방지 전략

1. **리소스 접근 순서 일관성 유지**:
   모든 트랜잭션이 동일한 순서로 리소스에 접근하도록 강제합니다. 예를 들어, 항상 상품 ID의 오름차순으로 락을 획득하도록 구현:

```kotlin
@Transactional
fun processOrder(order: Order) {
    // 상품 ID 기준 오름차순으로 정렬하여 항상 동일한 순서로 락 획득
    val sortedItems = order.items.sortedBy { it.productId }
    
    for (item in sortedItems) {
        val product = productRepository.findByIdWithLock(item.productId)
        // 상품 재고 처리 로직
    }
    
    // 주문 처리 로직
}
```

2. **타임아웃 설정**:
   락 획득 시도에 시간 제한을 설정하여 데드락 상황에서도 시스템이 무한정 멈추지 않도록 합니다.

```kotlin
// JPA 설정에서 락 타임아웃 설정
@Bean
fun entityManagerFactory() {
    val em = LocalContainerEntityManagerFactoryBean()
    // ...
    val properties = HashMap<String, Any>()
    properties["javax.persistence.lock.timeout"] = 5000 // 5초 타임아웃
    em.setJpaPropertyMap(properties)
    // ...
    return em
}
```

3. **데드락 감지 및 복구**:
   데이터베이스의 데드락 감지 기능을 활용하거나, 애플리케이션 수준에서 데드락을 감지하고 롤백 후 재시도하는 메커니즘을 구현합니다.

```kotlin
@Transactional(noRollbackFor = [DeadlockException::class])
fun executeWithDeadlockRetry(maxRetries: Int = 3, block: () -> Unit) {
    var retryCount = 0
    while (true) {
        try {
            block()
            break
        } catch (e: Exception) {
            if (isDeadlockException(e) && retryCount < maxRetries) {
                retryCount++
                Thread.sleep(100 * retryCount) // 재시도 간 지수 백오프
                continue
            }
            throw e
        }
    }
}
```

#### 적용 결과

위 전략들을 적용한 결과, 특히 리소스 접근 순서 일관성 유지 방법을 통해 주문 처리 과정에서 발생할 수 있는 데드락 위험을 효과적으로 제거할 수 있었습니다. 동시에 여러 주문이 처리되더라도 서로 교착 상태에 빠지지 않고 안정적으로 트랜잭션이 완료됩니다.

이는 비관적 락의 장점을 유지하면서도 그 단점인 데드락 위험을 최소화하는 균형 잡힌 방식으로, 시스템의 안정성과 데이터 일관성을 모두 확보할 수 있었습니다.
