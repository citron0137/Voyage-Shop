# 조회 성능 모니터링 구축 방안

## 개요

Voyage-Shop 애플리케이션의 조회 성능을 효과적으로 모니터링하기 위한 방안을 제시합니다. 이 문서는 성능 이슈를 사전에 감지하고, 병목 지점을 식별하며, 개선 효과를 측정하기 위한 모니터링 체계 구축 방법을 설명합니다.

## 모니터링 대상 조회 기능

앞서 식별된 주요 성능 이슈가 있는 조회 기능들:

1. **주문 아이템 순위 조회** (`OrderItemRankFacade.getRecentTopOrderItemRanks()`)
2. **주문 목록 조회** (`OrderFacade.getAllOrders()`, `getOrdersByUserId()`)
3. **쿠폰 사용자 정보 조회** (`CouponUserService.getAllCouponUsers()`, `getAllCouponsByUserId()`)
4. **상품 전체 조회** (`ProductFacade.getAllProducts()`)
5. **락(Lock)을 사용하는 조회 기능** (`findByUserIdWithLock()`, `findByIdWithLock()` 등)

## 주요 모니터링 지표 (메트릭)

### 1. 응답 시간 메트릭

- **평균 응답 시간**: 각 조회 API의 평균 처리 시간
- **응답 시간 백분위**: P50(중앙값), P90, P95, P99 응답 시간
- **최대 응답 시간**: 가장 오래 걸린 요청의 응답 시간
- **응답 시간 분포**: 응답 시간별 요청 분포도

### 2. 데이터베이스 메트릭

- **쿼리 실행 시간**: 개별 SQL 쿼리의 실행 시간
- **DB 연결 풀 사용률**: 활성/유휴 연결 수
- **쿼리 호출 횟수**: API 요청 당 발생하는 SQL 쿼리 수 (N+1 문제 탐지)
- **락 획득 대기 시간**: 데이터베이스 락 획득을 위한 대기 시간
- **락 보유 시간**: 트랜잭션이 락을 보유하는 시간

### 3. 시스템 리소스 메트릭

- **CPU 사용률**: 조회 요청 처리 시 CPU 사용량
- **메모리 사용량**: 힙 메모리 사용량 및 GC 활동
- **스레드 상태**: 활성/대기 스레드 수
- **네트워크 I/O**: 네트워크 트래픽 양

### 4. 애플리케이션 메트릭

- **요청 처리량(Throughput)**: 초당 처리 요청 수
- **동시 사용자 수**: 동시에 시스템을 사용하는 사용자 수
- **오류율**: 요청 대비 오류 발생률
- **캐시 히트율**: 캐시 사용 시 캐시 히트율

## 모니터링 구현 방안

### 1. 애플리케이션 수준 모니터링 (단기)

#### 1.1 AOP를 활용한 성능 측정

```kotlin
@Aspect
@Component
class PerformanceMonitoringAspect(
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    @Around("@annotation(Monitored)")
    fun monitorPerformance(joinPoint: ProceedingJoinPoint): Any {
        val methodName = joinPoint.signature.name
        val className = joinPoint.signature.declaringTypeName
        val timer = meterRegistry.timer("api.response.time", 
            "class", className, "method", methodName)
        
        return timer.record<Any> {
            try {
                joinPoint.proceed()
            } catch (e: Exception) {
                meterRegistry.counter("api.error", 
                    "class", className, "method", methodName).increment()
                throw e
            }
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Monitored
```

#### 1.2 모니터링 적용 예시

```kotlin
@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    @Monitored
    @Transactional(readOnly = true)
    fun getProduct(id: String): Product {
        return productRepository.findById(id)
            ?: throw ProductException.NotFound("Product with id: $id")
    }
    
    @Monitored
    @Transactional(readOnly = true)
    fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }
}
```

### 2. 데이터베이스 쿼리 모니터링 (중기)

#### 2.1 P6Spy 설정

```kotlin
// application.yml
p6spy:
  enabled: true
  appender: com.p6spy.engine.spy.appender.Slf4JLogger
  logMessageFormat: com.p6spy.engine.spy.appender.CustomLineFormat
  customLogMessageFormat: "executionTime:%(executionTime)ms | sql: %(sql)"
```

#### 2.2 Hibernate 통계 활성화

```kotlin
// HibernateConfig.kt
@Configuration
class HibernateConfig {
    @Bean
    fun hibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { properties ->
            properties["hibernate.generate_statistics"] = "true"
            properties["hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS"] = "100"
        }
    }
}
```

### 3. 모니터링 시스템 구축 (중기~장기)

#### 3.1 Micrometer + Prometheus + Grafana 구성

```kotlin
// PrometheusMeterRegistryConfig.kt
@Configuration
class PrometheusMeterRegistryConfig {
    
    @Bean
    fun prometheusRegistry(applicationContext: ApplicationContext): PrometheusMeterRegistry {
        val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        
        // 응답 시간 백분위 히스토그램 설정
        registry.config().meterFilter(
            MeterFilter.maxExpected("api.response.time", Duration.ofSeconds(10))
        )
        
        // DB 커넥션 풀 모니터링
        registry.gauge("db.connections.active", 
            applicationContext.getBean(HikariDataSource::class.java)) { it.hikariPoolMXBean.activeConnections }
        registry.gauge("db.connections.idle", 
            applicationContext.getBean(HikariDataSource::class.java)) { it.hikariPoolMXBean.idleConnections }
        
        return registry
    }
    
    @Bean
    fun prometheusScrapeEndpoint(registry: PrometheusMeterRegistry): RouterFunction<ServerResponse> {
        return RouterFunctions.route()
            .GET("/actuator/prometheus") { 
                ServerResponse.ok().body(registry.scrape()) 
            }
            .build()
    }
}
```

#### 3.2 Grafana 대시보드 설정 (JSON 예시)

```json
{
  "dashboard": {
    "title": "Voyage-Shop Performance Dashboard",
    "panels": [
      {
        "title": "API Response Time (P95)",
        "type": "graph",
        "datasource": "Prometheus",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, sum(rate(api_response_time_seconds_bucket[5m])) by (le, method))",
            "legendFormat": "{{method}} - P95"
          }
        ]
      },
      {
        "title": "Database Query Execution Time",
        "type": "graph",
        "datasource": "Prometheus",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, sum(rate(hibernate_query_execution_time_ms_bucket[5m])) by (le))",
            "legendFormat": "Query Time P95"
          }
        ]
      },
      {
        "title": "Lock Wait Time",
        "type": "graph",
        "datasource": "Prometheus",
        "targets": [
          {
            "expr": "avg(lock_wait_time_ms) by (repository)",
            "legendFormat": "{{repository}}"
          }
        ]
      }
    ]
  }
}
```

### 4. 분산 추적 시스템 구축 (장기)

#### 4.1 Spring Cloud Sleuth + Zipkin 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin")
}

// application.yml
spring:
  sleuth:
    sampler:
      probability: 1.0  # 개발 환경에서는 1.0, 프로덕션 환경에서는 조정 필요
  zipkin:
    base-url: http://localhost:9411
```

#### 4.2 분산 추적 정보 활용

```kotlin
@Service
class TracedProductService(
    private val productRepository: ProductRepository
) {
    @Transactional(readOnly = true)
    fun getProduct(id: String): Product {
        val span = tracer.nextSpan().name("getProduct").start()
        
        try {
            span.tag("productId", id)
            
            val product = productRepository.findById(id)
                ?: throw ProductException.NotFound("Product with id: $id")
            
            return product
        } finally {
            span.finish()
        }
    }
}
```

## 성능 임계값 및 알림 설정

### 1. 응답 시간 임계값 (SLO 기반)

| 조회 기능 | 정상 응답 시간 | 경고 임계값 | 심각 임계값 |
|----------|--------------|-----------|-----------|
| 단일 상품 조회 | < 100ms | > 300ms | > 1000ms |
| 상품 목록 조회 | < 500ms | > 1000ms | > 3000ms |
| 주문 내역 조회 | < 200ms | > 500ms | > 2000ms |
| 주문 상품 순위 조회 | < 1000ms | > 3000ms | > 10000ms |
| 쿠폰 목록 조회 | < 300ms | > 700ms | > 2000ms |

### 2. 알림 설정 예시 (Prometheus Alertmanager rules.yml)

```yaml
groups:
- name: voyage-shop-alerts
  rules:
  - alert: ApiHighResponseTime
    expr: histogram_quantile(0.95, sum(rate(api_response_time_seconds_bucket[5m])) by (le, method)) > 1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "API 높은 응답 시간: {{ $labels.method }}"
      description: "{{ $labels.method }} API의 P95 응답 시간이 1초를 초과했습니다."

  - alert: HighDatabaseConnections
    expr: sum(db_connections_active) > 80
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "높은 DB 연결 사용량"
      description: "활성 DB 연결이 80%를 초과했습니다."

  - alert: HighLockWaitTime
    expr: avg(lock_wait_time_ms) > 200
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "높은 락 대기 시간"
      description: "평균 락 대기 시간이 200ms를 초과했습니다."
```

## 모니터링 데이터 분석 및 활용

### 1. 성능 개선 판단 기준

- **기준값 대비 개선율**: 개선 전/후 응답 시간 비교
- **트렌드 분석**: 시간 경과에 따른 성능 변화 추이
- **부하 테스트 결과**: 다양한 부하 상황에서의 성능 지표

### 2. 주기적 성능 리포트 생성

```kotlin
@Component
@Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 자정
class PerformanceReportGenerator(
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    fun generateWeeklyReport() {
        val report = StringBuilder()
        report.append("=== 주간 성능 리포트 ===\n")
        
        // API 응답 시간 통계
        val apiTimers = meterRegistry.find("api.response.time").timers()
        apiTimers.forEach { timer ->
            report.append("API: ${timer.id.getTag("method")}\n")
            report.append("  평균 응답 시간: ${timer.mean(TimeUnit.MILLISECONDS)}ms\n")
            report.append("  P95 응답 시간: ${timer.takeSnapshot().percentileValues()[4].value(TimeUnit.MILLISECONDS)}ms\n")
        }
        
        // 데이터베이스 통계
        report.append("\n=== 데이터베이스 통계 ===\n")
        val dbConnectionsActive = meterRegistry.find("db.connections.active").gauge()?.value() ?: 0.0
        report.append("활성 DB 연결: $dbConnectionsActive\n")
        
        // 주요 성능 이슈 탐지
        report.append("\n=== 성능 이슈 탐지 ===\n")
        val slowApis = apiTimers.filter { it.mean(TimeUnit.MILLISECONDS) > 1000 }
        slowApis.forEach { timer ->
            report.append("느린 API 발견: ${timer.id.getTag("method")}, 평균 응답 시간: ${timer.mean(TimeUnit.MILLISECONDS)}ms\n")
        }
        
        logger.info(report.toString())
        // 이메일 발송 또는 성능 리포트 저장 로직
    }
}
```

### 3. 성능 데이터 시각화 도구

- **Grafana**: 실시간 대시보드 및 트렌드 분석
- **Kibana**: 로그 기반 성능 분석 (ELK 스택 활용 시)
- **Custom Dashboard**: 비즈니스 KPI와 성능 지표 연계

## 구현 단계별 계획

### 단기 (1-2주)

1. **기본 메트릭 수집 설정**
   - Spring Boot Actuator 활성화
   - Micrometer 설정
   - 주요 API에 @Monitored 애너테이션 적용

2. **로깅 기반 모니터링**
   - 느린 쿼리 로깅 설정
   - 주요 API 응답 시간 로깅
   - 오류 상황 상세 로깅

### 중기 (2-4주)

1. **Prometheus + Grafana 환경 구축**
   - 메트릭 수집을 위한 Prometheus 설정
   - 기본 대시보드 구성
   - 주요 메트릭에 대한 알림 규칙 설정

2. **데이터베이스 모니터링 강화**
   - 쿼리 성능 추적
   - 커넥션 풀 모니터링
   - 락 관련 메트릭 수집

### 장기 (1-2개월)

1. **분산 추적 시스템 도입**
   - Zipkin 서버 구축
   - Spring Cloud Sleuth 연동
   - 주요 서비스 간 트레이싱 설정

2. **고급 모니터링 및 자동화**
   - 자동 성능 리포트 생성
   - 비정상 패턴 자동 감지
   - 머신러닝 기반 이상 탐지 시스템 구축

## 결론

Voyage-Shop 애플리케이션의 조회 성능을 효과적으로 모니터링하기 위해서는 단계적인 접근이 필요합니다. 단기적으로는 기본적인 메트릭 수집과 로깅 체계를 갖추고, 중기적으로는 전문 모니터링 도구를 도입하며, 장기적으로는 분산 추적과 자동화된 성능 관리 시스템을 구축해야 합니다.

적절한 모니터링 체계가 구축되면 성능 이슈를 조기에 발견하고, 개선 작업의 효과를 정량적으로 측정할 수 있으며, 데이터 기반의 의사결정을 통해 지속적인 성능 최적화가 가능해집니다.

끝으로, 모니터링은 한 번 구축하고 끝나는 것이 아니라 지속적으로 개선해야 하는 과정임을 인식하고, 시스템 변화와 비즈니스 요구사항에 맞춰 모니터링 체계도 함께 발전시켜 나가야 합니다. 