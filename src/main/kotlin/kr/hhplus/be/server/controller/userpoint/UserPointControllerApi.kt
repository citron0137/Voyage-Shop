package kr.hhplus.be.server.controller.userpoint

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 사용자 포인트 API
 * 사용자 포인트 조회 및 충전/사용 기능을 제공합니다.
 */
@Tag(name = "사용자 포인트 API", description = "사용자 포인트 조회 및 충전/사용 기능을 제공하는 API")
@RequestMapping("/api/v1/user-points")
interface UserPointControllerApi {

    /**
     * 사용자의 포인트 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 포인트 정보
     */
    @Operation(
        summary = "사용자 포인트 조회",
        description = "사용자 ID로 해당 사용자의 포인트 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "포인트 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/UserPointResponse.Single")
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "사용자 ID가 빈 값인 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없는 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @GetMapping("/{userId}")
    fun getUserPoint(
        @Parameter(description = "조회할 사용자 ID", required = true)
        @PathVariable userId: String
    ): BaseResponse<UserPointResponse.Single>

    /**
     * 사용자의 포인트를 충전합니다.
     *
     * @param userId 사용자 ID
     * @param request 포인트 충전 요청
     * @return 충전 후 사용자 포인트 정보
     */
    @Operation(
        summary = "포인트 충전",
        description = "사용자 ID로 해당 사용자의 포인트를 충전합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "포인트 충전 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/UserPointResponse.Single")
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "충전 금액이 유효하지 않은 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없는 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @PostMapping("/{userId}/charge")
    fun chargePoint(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable userId: String,
        @Parameter(description = "충전 요청 정보", required = true)
        @RequestBody request: UserPointRequest.Charge
    ): BaseResponse<UserPointResponse.Single>
} 