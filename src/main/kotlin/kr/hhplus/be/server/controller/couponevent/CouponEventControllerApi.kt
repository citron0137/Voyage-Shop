package kr.hhplus.be.server.controller.couponevent

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

/**
 * 쿠폰 이벤트 API
 */
@Tag(name = "쿠폰 이벤트 API", description = "쿠폰 이벤트 관련 API")
@RequestMapping("/api/v1")
interface CouponEventControllerApi {

    /**
     * 쿠폰 이벤트를 생성합니다.
     *
     * @param req 쿠폰 이벤트 생성 요청
     * @return 생성된 쿠폰 이벤트 정보
     */
    @Operation(
        summary = "쿠폰 이벤트 생성",
        description = "새로운 쿠폰 이벤트를 생성합니다. 가능한 에러 코드:\n" +
                "- COUPON_EVENT_INVALID_BENEFIT_METHOD: 지원하지 않는 혜택 방식이 입력된 경우\n" +
                "- COUPON_EVENT_INVALID_BENEFIT_AMOUNT: 유효하지 않은 혜택 금액(음수 등)이 입력된 경우\n" +
                "- COUPON_EVENT_INVALID_ISSUE_AMOUNT: 유효하지 않은 발급 수량(0 이하 등)이 입력된 경우"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성공 또는 에러 응답",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/CouponEventResponse.Event")
                )]
            )
        ]
    )
    @PostMapping("/coupon-events")
    fun createCouponEvent(
        @Parameter(description = "쿠폰 이벤트 생성 요청 정보", required = true)
        @RequestBody req: CouponEventRequest.Create
    ): BaseResponse<CouponEventResponse.Event>

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     *
     * @return 쿠폰 이벤트 목록
     */
    @Operation(
        summary = "모든 쿠폰 이벤트 조회",
        description = "시스템에 등록된 모든 쿠폰 이벤트를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/CouponEventResponse.List")
                )]
            )
        ]
    )
    @GetMapping("/coupon-events")
    fun getAllCouponEvents(): BaseResponse<List<CouponEventResponse.Event>>

    /**
     * 사용자에게 쿠폰을 발급합니다.
     *
     * @param couponEventId 쿠폰 이벤트 ID
     * @param req 쿠폰 발급 요청
     * @return 발급된 쿠폰 정보
     */
    @Operation(
        summary = "쿠폰 발급",
        description = "특정 쿠폰 이벤트에서 사용자에게 쿠폰을 발급합니다. 가능한 에러 코드:\n" +
                "- COUPON_EVENT_NOT_FOUND: 지정된 ID의 쿠폰 이벤트가 존재하지 않는 경우\n" +
                "- COUPON_EVENT_OUT_OF_STOCK: 쿠폰 재고가 부족한 경우\n" +
                "- COUPON_EVENT_STOCK_EMPTY: 쿠폰 재고가 완전히 소진된 경우"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성공 또는 에러 응답",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/CouponEventResponse.IssueCoupon")
                )]
            )
        ]
    )
    @PostMapping("/coupon-events/{couponEventId}/issue-coupon-user")
    fun issueCouponUser(
        @Parameter(description = "쿠폰 이벤트 ID", required = true)
        @PathVariable couponEventId: String,
        @Parameter(description = "쿠폰 발급 요청 정보", required = true)
        @RequestBody req: CouponEventRequest.IssueCoupon
    ): BaseResponse<CouponEventResponse.IssueCoupon>
} 