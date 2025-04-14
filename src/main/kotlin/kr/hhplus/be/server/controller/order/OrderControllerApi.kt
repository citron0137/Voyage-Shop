package kr.hhplus.be.server.controller.order

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
 * 주문 API
 */
@Tag(name = "주문 API", description = "주문 관련 API")
@RequestMapping("/api/v1")
interface OrderControllerApi {

    /**
     * 주문을 생성합니다.
     *
     * @param req 주문 생성 요청 DTO
     * @return 생성된 주문 정보
     */
    @Operation(
        summary = "주문 생성",
        description = "새로운 주문을 생성합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "주문 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/OrderResponse")
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @PostMapping("/orders")
    fun createOrder(
        @Parameter(description = "주문 생성 요청 정보", required = true)
        @RequestBody req: OrderRequest.Create
    ): BaseResponse<OrderResponse.Order>
} 