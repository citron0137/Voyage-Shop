# 컨트롤러 레이어 규약

## 1. 개요

본 문서는 Voyage-Shop 애플리케이션의 컨트롤러(Controller) 레이어 개발 시 일관성을 유지하고, 코드 가독성 및 유지보수성을 향상시키기 위한 규약을 정의합니다. 모든 개발자는 본 규약을 숙지하고 준수하는 것을 원칙으로 합니다.

## 2. 디렉토리 구조

```
/controller
  /도메인명/
    DomainControllerApi.kt   # API 인터페이스
    DomainController.kt      # 구현체
    DomainResponse.kt        # 응답 객체
  /shared/
    /exception/
      GlobalExceptionHandler.kt  # 일반 예외 처리
      DomainExceptionHandler.kt  # 도메인 예외 처리
    /swagger/
      CommonSwaggerSchema.kt     # 공통 스키마 정의
      SchemaProvider.kt          # 스키마 제공 인터페이스
    BaseResponse.kt              # 공통 응답 형식
```

각 도메인별로 독립된 패키지를 생성하고, 그 안에 관련 컨트롤러와 응답 객체를 배치합니다.

## 3. API 인터페이스 (XXXControllerApi)

### 3.1 기본 구조

```kotlin
@Tag(name = "도메인명 API", description = "도메인 기능 설명")
@RequestMapping("/도메인-경로")
interface DomainControllerApi {
    // API 메서드 선언
}
```

### 3.2 API 문서화

모든 API 메서드는 다음 어노테이션을 통해 문서화합니다:

- `@Operation`: API 요약 및 상세 설명
- `@ApiResponses`: 가능한 응답 코드 및 설명
- `@Parameter`: 메서드 파라미터 설명
- 클래스와 메서드에 코틀린 주석(KDoc) 추가

예시:
```kotlin
/**
 * 사용자 조회 API
 *
 * @param userId 조회할 사용자 ID
 * @return 조회된 사용자 정보
 */
@Operation(
    summary = "사용자 조회",
    description = "사용자 ID를 이용하여 특정 사용자 정보를 조회합니다."
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "사용자 조회 성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = Any::class, ref = "#/components/schemas/BaseResponse")
            )]
        ),
        ApiResponse(
            responseCode = "200",
            description = "사용자를 찾을 수 없는 경우 (U_NOT_FOUND)",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = Any::class, ref = "#/components/schemas/BaseResponse")
            )]
        )
    ]
)
@GetMapping("/{userId}")
fun getUserById(
    @Parameter(description = "조회할 사용자 ID", required = true)
    @PathVariable userId: String
): BaseResponse<UserResponse.Single>
```

### 3.3 HTTP 메서드 매핑

- 조회: `@GetMapping`
- 생성: `@PostMapping` 
- 전체 수정: `@PutMapping` 
- 부분 수정: `@PatchMapping`
- 삭제: `@DeleteMapping`

## 4. 컨트롤러 구현체 (XXXController)

### 4.1 기본 구조

```kotlin
@RestController
class DomainController(
    private val domainFacade: DomainFacade
) : DomainControllerApi {
    // API 메서드 구현
}
```

### 4.2 구현 원칙

1. **간결함**: 컨트롤러 메서드는 가능한 한 간결하게 유지
2. **책임 분리**: 비즈니스 로직은 Facade나 Service로 위임
3. **일관된 응답**: `BaseResponse`를 통한 일관된 응답 형식 제공
4. **Facade 패턴 활용**: 여러 서비스를 조합해야 하는 경우 Facade 계층 활용

예시:
```kotlin
override fun getUserById(userId: String): BaseResponse<UserResponse.Single> {
    val criteria = UserCriteria.GetById(userId)
    val result = userFacade.findUserById(criteria)
    return BaseResponse.success(UserResponse.Single.from(result))
}
```

## 5. 응답 객체 (XXXResponse)

### 5.1 기본 구조

```kotlin
sealed class DomainResponse {
    // 단일 항목 응답
    data class Single(
        // 필드 정의
    ) : DomainResponse() {
        companion object {
            fun from(result: DomainResult.Item): Single {
                // 매핑 로직
            }
        }
    }

    // 목록 응답
    data class List(
        val items: kotlin.collections.List<Single>
    ) : DomainResponse() {
        companion object {
            fun from(result: DomainResult.List): List {
                // 매핑 로직
            }
        }
    }
}
```

### 5.2 응답 객체 설계 원칙

1. **도메인별 독립성**: 각 도메인별 응답 객체 정의
2. **변환 메서드**: 도메인 결과에서 응답 객체로 변환하는 `from` 메서드 제공
3. **타입 안전성**: sealed class를 활용한 응답 타입 안전성 확보
4. **중첩 구조**: 단일 항목과 목록을 위한 중첩 클래스 구조 활용
5. **엔티티 노출 금지**: 도메인 엔티티를 직접 반환하지 않고 항상 응답 객체로 변환

## 6. 공통 응답 형식 (BaseResponse)

모든 API 응답은 다음 형식을 따릅니다:

```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

또는 오류의 경우:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "오류_코드",
    "message": "오류 메시지"
  }
}
```

### 6.1 응답 생성 헬퍼 메서드

- 성공 응답: `BaseResponse.success(data)`
- 실패 응답: `BaseResponse.fail(code, message)`

## 7. URL 경로 설계

1. **REST 원칙 준수**: 리소스 중심의 URL 설계
2. **복수형 사용**: 리소스명은 복수형으로 표현 (`/users`, `/products`)
3. **케밥 케이스**: 다중 단어 경로는 케밥 케이스 사용 (`/order-items`)
4. **경로 변수**: 리소스 식별자는 경로 변수로 표현 (`/users/{userId}`)
5. **쿼리 파라미터**: 필터링, 정렬, 페이징에는 쿼리 파라미터 활용

예시:

```
GET /users                     # 사용자 목록 조회
GET /users/{userId}            # 특정 사용자 조회
POST /users                    # 사용자 생성
PUT /users/{userId}            # 사용자 정보 전체 수정
PATCH /users/{userId}          # 사용자 정보 부분 수정
DELETE /users/{userId}         # 사용자 삭제
GET /users/{userId}/orders     # 특정 사용자의 주문 목록 조회
```

## 8. 예외 처리

### 8.1 예외 처리 전략

모든 예외는 `@RestControllerAdvice`를 사용한 전역 예외 핸들러에서 처리합니다. 예외 처리는 다음 두 그룹으로 나뉩니다:

1. **도메인 예외**: 비즈니스 규칙 위반, 리소스 미존재 등 도메인 관련 예외
   - `AbstractDomainException`의 하위 클래스로 정의
   - 항상 HTTP 상태 코드 200으로 응답하며, 응답 내 `success: false`로 전달
   - `DomainExceptionHandler`에서 일괄 처리

2. **일반 예외**: 시스템 오류, 잘못된 요청 형식 등 기술적 예외
   - 주로 프레임워크 예외나 `RuntimeException` 계열
   - 적절한 HTTP 상태 코드로 응답 (400, 500 등)
   - `GlobalExceptionHandler`에서 일괄 처리

### 8.2 도메인 예외 처리

```kotlin
@ExceptionHandler(AbstractDomainException::class)
@ResponseStatus(HttpStatus.OK)
fun handleDomainException(ex: AbstractDomainException): BaseResponse<Nothing> {
    log.warn("도메인 예외 발생: [${ex.errorCode}] ${ex.errorMessage}")
    return BaseResponse.fail(ex.errorCode, ex.errorMessage)
}
```

### 8.3 일반 예외 처리

```kotlin
@ExceptionHandler(MethodArgumentTypeMismatchException::class)
@ResponseStatus(HttpStatus.OK)
fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException): BaseResponse<Nothing> {
    log.warn("잘못된 요청 파라미터: ${ex.message}")
    return BaseResponse.fail("G_INVALID_PARAM", "잘못된 ${ex.name} 값입니다: ${ex.value}")
}

@ExceptionHandler(RuntimeException::class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
fun handleRuntimeException(ex: RuntimeException): BaseResponse<Nothing> {
    log.error("런타임 예외 발생", ex)
    return BaseResponse.fail("G_SERVER_ERROR", "서버 오류가 발생했습니다")
}
```

### 8.4 오류 코드 규칙

오류 코드는 `도메인_오류유형` 형식으로 정의합니다:

- 사용자 도메인: `U_`로 시작 (예: `U_NOT_FOUND`)
- 주문 도메인: `O_`로 시작 (예: `O_INVALID_STATUS`)
- 일반/공통 오류: `G_`로 시작 (예: `G_SERVER_ERROR`)

## 9. API 문서화 (Swagger/OpenAPI)

### 9.1 스키마 정의

각 도메인은 `SchemaProvider` 인터페이스를 구현하여 API 응답 스키마를 정의합니다:

```kotlin
@Component
class UserSchemaProvider : SchemaProvider {
    override fun getSchemas(): Map<String, Schema<Any>> {
        return mapOf(
            "UserResponseSingle" to createUserResponseSingleSchema(),
            "UserResponseList" to createUserResponseListSchema()
        )
    }
    
    private fun createUserResponseSingleSchema(): Schema<Any> {
        // 스키마 정의 로직
    }
}
```

### 9.2 문서화 규칙

1. **모든 API에 설명 추가**: `@Operation`, `@ApiResponses`, `@Parameter` 사용
2. **예제 값 제공**: 가능한 경우 `@Schema(example = "...")` 사용
3. **모든 응답 코드 문서화**: 성공 응답 뿐만 아니라 가능한 모든 오류 응답 문서화
4. **일관된 태그 사용**: 도메인별로 일관된 태그 사용 (`@Tag`)

## 10. 입력 유효성 검증

1. **도메인 검증**: 모든 비즈니스 규칙 관련 검증은 도메인/서비스 계층에서 수행
2. **기본 검증**: 입력값의 기본 유효성(필수값, 형식 등)은 컨트롤러에서 수행
3. **입력 제한**: 필요한 경우 `@RequestBody`, `@PathVariable`, `@RequestParam`에 검증 조건 지정

## 11. 컨트롤러 테스트

### 11.1 테스트 대상

컨트롤러 테스트는 다음 두 수준으로 작성합니다:

1. **단위 테스트**: 각 컨트롤러 메서드에 대한 단위 테스트 (Facade 계층을 Mocking)
2. **통합 테스트**: API 엔드포인트에 대한 End-to-End 테스트 (`@SpringBootTest`)

### 11.2 테스트 네이밍 규칙

- 단위 테스트: `[메서드명]_[시나리오]_[기대결과]`
- 통합 테스트: `[HTTP메서드]_[경로]_[시나리오]_[기대결과]`

예시:
```kotlin
// 단위 테스트
@Test
fun getUserById_validId_returnsUser() { ... }

// 통합 테스트
@Test
fun get_usersById_validId_returns200AndUserData() { ... }
```

## 12. 보안

본 프로젝트에서는 별도의 인증 및 보안 처리를 컨트롤러 레이어에서 구현하지 않습니다. 필요한 경우 추후 별도의 보안 모듈이나 미들웨어를 통해 구현할 예정입니다. 