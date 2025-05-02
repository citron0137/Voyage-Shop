package kr.hhplus.be.server.shared.lock

import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 분산 락 어노테이션(@DistributedLock)을 처리하는 AOP Aspect
 * 트랜잭션 시작 전에 락을 획득하기 위해 높은 우선순위로 설정
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class DistributedLockAspect(private val lockManager: DistributedLockManager) {
    
    /**
     * @DistributedLock 어노테이션이 적용된 메서드 실행을 가로채서
     * 락을 획득한 후 메서드를 실행하고 락을 해제합니다.
     *
     * @param joinPoint 메서드 실행 지점
     * @return 메서드 실행 결과
     */
    @Around("@annotation(kr.hhplus.be.server.shared.lock.DistributedLock) && execution(* *(..))")
    fun executeWithLock(joinPoint: ProceedingJoinPoint): Any {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)
        
        val actualKey = resolveKey(distributedLock.key, distributedLock.parameterName, joinPoint)
        
        return lockManager.executeWithLock(
            key = actualKey,
            timeout = distributedLock.timeout,
            unit = distributedLock.timeUnit
        ) {
            joinPoint.proceed()
        }
    }
    
    /**
     * 락 키를 해석합니다. 파라미터 이름이 제공된 경우 해당 파라미터 값을 추출하여 키에 추가합니다.
     *
     * @param keyPrefix 락 키 접두사
     * @param parameterName 파라미터 이름
     * @param joinPoint 메서드 실행 지점
     * @return 실제 락 키
     */
    private fun resolveKey(keyPrefix: String, parameterName: String, joinPoint: ProceedingJoinPoint): String {
        if (parameterName.isBlank()) return keyPrefix
        
        val method = (joinPoint.signature as MethodSignature).method
        val parameterNames = method.parameters.map { it.name }
        val args = joinPoint.args
        
        // 파라미터 이름에서 중첩 경로 처리 (예: command.userId)
        val parts = parameterName.split(".")
        val rootParamName = parts[0]
        
        // 파라미터 인덱스 찾기
        val paramIndex = parameterNames.indexOf(rootParamName)
        if (paramIndex == -1) {
            throw IllegalArgumentException("파라미터 이름 '$rootParamName'을 찾을 수 없습니다")
        }
        
        // 파라미터 값 가져오기
        var value: Any? = args[paramIndex]
        
        // 중첩 필드 처리
        for (i in 1 until parts.size) {
            if (value == null) break
            
            val fieldName = parts[i]
            val getterMethod = value::class.java.methods.find { 
                it.name == "get${fieldName.capitalize()}" || 
                it.name == fieldName 
            }
            
            value = getterMethod?.invoke(value)
        }
        
        return "$keyPrefix:$value"
    }
} 