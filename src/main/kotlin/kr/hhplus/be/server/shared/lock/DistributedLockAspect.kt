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
import java.util.concurrent.TimeUnit

/**
 * 분산 락 어노테이션(@DistributedLock, @CompositeLock)을 처리하는 AOP Aspect
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
    @Around("@annotation(kr.hhplus.be.server.shared.lock.DistributedLock)")
    fun executeWithLock(joinPoint: ProceedingJoinPoint): Any {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val resourceId = resolveResourceId(distributedLock.resourceIdExpression, joinPoint)

        // LockKeyGenerator 활용 (executeWithDomainLock 사용)
        return lockManager.executeWithDomainLock(
            domainPrefix = distributedLock.domain,
            resourceType = distributedLock.resourceType,
            resourceId = resourceId,
            timeout = distributedLock.timeout,
            unit = distributedLock.timeUnit
        ) {
            joinPoint.proceed()
        }
    }
    
    /**
     * @CompositeLock 어노테이션이 적용된 메서드 실행을 가로채서
     * 여러 락을 순서대로 획득한 후 메서드를 실행하고 락을 해제합니다.
     *
     * @param joinPoint 메서드 실행 지점
     * @return 메서드 실행 결과
     */
    @Around("@annotation(kr.hhplus.be.server.shared.lock.CompositeLock)")
    fun executeWithCompositeLock(joinPoint: ProceedingJoinPoint): Any {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val compositeLock = method.getAnnotation(CompositeLock::class.java)

        // 각 락에 대한 도메인 리소스 준비
        val domainResources = compositeLock.locks.map { lock ->
            val resourceId = resolveResourceId(lock.resourceIdExpression, joinPoint)
            Triple(lock.domain, lock.resourceType, resourceId)
        }
        
        // DistributedLockManager 확장 함수 활용
        return lockManager.withDomainLocks(
            domainResources = domainResources,
            ordered = compositeLock.ordered,
            action = {
                joinPoint.proceed()
            }
        )
    }
    
    /**
     * 표현식에서 리소스 ID를 추출합니다.
     * 예: "criteria.userId"와 같은 표현식에서 실제 userId 값을 추출
     *
     * @param expression 표현식
     * @param joinPoint 메서드 실행 지점
     * @return 추출된 리소스 ID 문자열
     */
    private fun resolveResourceId(expression: String, joinPoint: ProceedingJoinPoint): String {
        val methodSignature = joinPoint.signature as MethodSignature
        val parameterNames = methodSignature.parameterNames
        val args = joinPoint.args
        
        // 표현식이 상수 문자열인 경우 (따옴표로 시작하는 경우)
        if (expression.startsWith("'") && expression.endsWith("'")) {
            return expression.substring(1, expression.length - 1)
        }
        
        // SpEL 표현식 처리 (예: #criteria.days)
        if (expression.startsWith("#")) {
            return resolveSpelExpression(expression.substring(1), parameterNames, args)
        }
        
        // 표현식에서 파라미터 경로 파싱 (예: "criteria.userId")
        val parts = expression.split(".")
        val rootParamName = parts[0]
        
        // 파라미터 인덱스 찾기
        val paramIndex = parameterNames.indexOf(rootParamName)
        if (paramIndex == -1) {
            throw IllegalArgumentException("파라미터 이름 '$rootParamName'을 찾을 수 없습니다: $expression")
        }
        
        // 파라미터 값 가져오기
        var value: Any? = args[paramIndex]
        
        // 중첩 필드 처리
        for (i in 1 until parts.size) {
            if (value == null) break
            
            val fieldName = parts[i]
            val getterMethod = value::class.java.methods.find { 
                it.name == "get${fieldName.capitalize()}" || 
                it.name == fieldName || 
                it.name == "is${fieldName.capitalize()}"
            }
            
            if (getterMethod == null) {
                throw IllegalArgumentException("필드 '$fieldName'을 클래스 '${value::class.java.simpleName}'에서 찾을 수 없습니다: $expression")
            }
            
            value = getterMethod.invoke(value)
        }
        
        return value?.toString() ?: throw IllegalArgumentException("표현식 '$expression'에서 null 값이 추출되었습니다")
    }

    /**
     * SpEL 표현식을 해석합니다.
     * 예: "#criteria.days + '_limit_' + #criteria.limit"와 같은 복합 표현식 처리
     * 
     * @param spelExpression SpEL 표현식
     * @param parameterNames 메서드 파라미터 이름 배열
     * @param args 메서드 파라미터 값 배열
     * @return 해석된 문자열
     */
    private fun resolveSpelExpression(spelExpression: String, parameterNames: Array<String>, args: Array<Any>): String {
        // + 연산자로 문자열 연결 처리
        if (spelExpression.contains("+")) {
            val parts = spelExpression.split("+").map { it.trim() }
            return parts.joinToString("") {
                if (it.startsWith("'") && it.endsWith("'")) {
                    // 문자열 리터럴 처리
                    it.substring(1, it.length - 1)
                } else if (it.startsWith("#")) {
                    // 재귀적으로 표현식 처리
                    resolveSpelExpression(it.substring(1), parameterNames, args)
                } else {
                    // 단순 변수 참조
                    resolveSimpleReference(it, parameterNames, args)
                }
            }
        }
        
        // 단순 변수 참조 처리
        return resolveSimpleReference(spelExpression, parameterNames, args)
    }
    
    /**
     * 단순 변수 참조를 해석합니다.
     * 예: "criteria.days" 같은 단순 경로 표현식
     */
    private fun resolveSimpleReference(reference: String, parameterNames: Array<String>, args: Array<Any>): String {
        val parts = reference.split(".")
        val rootParamName = parts[0]
        
        val paramIndex = parameterNames.indexOf(rootParamName)
        if (paramIndex == -1) {
            throw IllegalArgumentException("파라미터 이름 '$rootParamName'을 찾을 수 없습니다: $reference")
        }
        
        var value: Any? = args[paramIndex]
        
        for (i in 1 until parts.size) {
            if (value == null) break
            
            val fieldName = parts[i]
            val getterMethod = value::class.java.methods.find { 
                it.name == "get${fieldName.capitalize()}" || 
                it.name == fieldName || 
                it.name == "is${fieldName.capitalize()}"
            }
            
            if (getterMethod == null) {
                throw IllegalArgumentException("필드 '$fieldName'을 클래스 '${value::class.java.simpleName}'에서 찾을 수 없습니다: $reference")
            }
            
            value = getterMethod.invoke(value)
        }
        
        return value?.toString() ?: throw IllegalArgumentException("표현식 '$reference'에서 null 값이 추출되었습니다")
    }
} 