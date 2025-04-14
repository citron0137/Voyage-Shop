package kr.hhplus.be.server.controller.orderitemrank

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*

/**
 * 주문 상품 순위 API
 */
@Tag(name = "주문 상품 순위 API", description = "주문 상품 순위 관련 API")
@RequestMapping("/api/v1")
interface OrderItemRankControllerApi {

    /**
     * 최근 주문 상품 순위를 조회합니다.
     *
     * @return 주문 상품 순위 목록
     */
    @Operation(
        summary = "주문 상품 순위 조회",
        description = "최근 주문 상품의 인기 순위를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/OrderItemRankResponse")
                )]
            )
        ]
    )
    @GetMapping("/order-item-rank")
    fun getOrderItemRank(): BaseResponse<List<OrderItemRankResponse.Rank>>
} 