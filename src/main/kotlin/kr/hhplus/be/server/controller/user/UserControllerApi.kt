package kr.hhplus.be.server.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 사용자 API
 * 사용자 생성 및 조회 기능을 제공합니다.
 */
@Tag(name = "사용자 API", description = "사용자 생성 및 조회 기능을 제공하는 API")
@RequestMapping("/users")
interface UserControllerApi {

    /**
     * 새로운 사용자를 생성합니다.
     *
     * @return 생성된 사용자 정보
     */
    @Operation(
        summary = "사용자 생성",
        description = "새로운 사용자를 생성합니다. 사용자 생성과 함께 해당 사용자의 포인트 정보도 함께 생성됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/BaseResponse")
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "사용자 ID가 빈 값인 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/BaseResponse")
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/BaseResponse")
                )]
            )
        ]
    )
    @PostMapping
    fun createUser(): BaseResponse<UserResponse.Single>

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
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
            ),
            ApiResponse(
                responseCode = "200",
                description = "사용자 ID가 빈 값인 경우 (U_INVALID_ID)",
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

    /**
     * 모든 사용자 목록을 조회합니다.
     *
     * @return 사용자 목록
     */
    @Operation(
        summary = "사용자 목록 조회",
        description = "모든 사용자 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 목록 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/BaseResponse")
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/BaseResponse")
                )]
            )
        ]
    )
    @GetMapping
    fun getAllUsers(): BaseResponse<UserResponse.List>
} 