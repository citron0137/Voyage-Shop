package kr.hhplus.be.server.controller.couponuser

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 쿠폰 사용자 API
 */
@Tag(name = "쿠폰 사용자 API", description = "쿠폰 사용자 관련 API")
@RequestMapping("/api/v1")
interface CouponUserControllerApi {

    /**
     * 모든 쿠폰 사용자를 조회합니다.
     *
     * @return 쿠폰 사용자 목록 응답
     */
    @Operation(
        summary = "모든 쿠폰 사용자 조회",
        description = "시스템에 등록된 모든 쿠폰 사용자 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/CouponUserResponse.List")
                )]
            )
        ]
    )
    @GetMapping("/coupon-users")
    fun getAllCouponUsers(): BaseResponse<List<CouponUserResponse.Single>>
} 