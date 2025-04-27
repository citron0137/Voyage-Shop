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
@Tag(name = "주문 API", description = "주문 생성 및 관리 API")
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
        description = "새로운 주문을 생성합니다. 주문 항목과 사용자 ID는 필수입니다. 쿠폰을 적용하여 할인받을 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "주문 생성 성공 또는 에러 응답",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/OrderResponse")
                )]
            ),
            ApiResponse(
                responseCode = "200", 
                description = "상품을 찾을 수 없는 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, example = """
                        {
                          "success": false,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "상품을 찾을 수 없습니다"
                          }
                        }
                    """)
                )]
            ),
            ApiResponse(
                responseCode = "200", 
                description = "재고가 부족한 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, example = """
                        {
                          "success": false,
                          "error": {
                            "code": "PRODUCT_STOCK_UNDERFLOW",
                            "message": "상품 재고가 부족합니다"
                          }
                        }
                    """)
                )]
            ),
            ApiResponse(
                responseCode = "200", 
                description = "주문 항목이 없는 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, example = """
                        {
                          "success": false,
                          "error": {
                            "code": "ORDER_FINAL_AMOUNT_INVALID",
                            "message": "주문 항목이 필요합니다"
                          }
                        }
                    """)
                )]
            ),
            ApiResponse(
                responseCode = "200", 
                description = "사용자를 찾을 수 없는 경우",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, example = """
                        {
                          "success": false,
                          "error": {
                            "code": "USER_NOT_FOUND",
                            "message": "사용자를 찾을 수 없습니다"
                          }
                        }
                    """)
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