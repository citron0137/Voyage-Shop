package kr.hhplus.be.server.controller.product

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
 * 상품 API
 * 상품 조회 및 관리 기능을 제공합니다.
 */
@Tag(name = "상품 API", description = "상품 조회 및 관리 기능을 제공하는 API")
@RequestMapping("/api/v1/products")
interface ProductControllerApi {

    /**
     * 상품 목록을 조회합니다.
     *
     * @return 상품 목록 정보
     */
    @Operation(
        summary = "상품 목록 조회",
        description = "전체 상품 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 목록 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/ProductResponse.List")
                )]
            )
        ]
    )
    @GetMapping
    fun getAllProducts(): BaseResponse<ProductResponse.List>

    /**
     * 상품을 조회합니다.
     *
     * @param productId 상품 ID
     * @return 상품 정보
     */
    @Operation(
        summary = "상품 조회",
        description = "상품 ID로 특정 상품의 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/ProductResponse.Single")
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품 ID가 빈 값인 경우 (PRODUCT_INVALID_ID)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품을 찾을 수 없는 경우 (PRODUCT_NOT_FOUND)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @GetMapping("/{productId}")
    fun getProduct(
        @Parameter(description = "조회할 상품 ID", required = true)
        @PathVariable productId: String
    ): BaseResponse<ProductResponse.Single>

    /**
     * 상품을 생성합니다.
     *
     * @param request 상품 생성 요청
     * @return 생성된 상품 정보
     */
    @Operation(
        summary = "상품 생성",
        description = "새로운 상품을 생성합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/ProductResponse.Single")
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품명이 빈 값인 경우 (PRODUCT_INVALID_NAME)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "가격이 0 이하인 경우 (PRODUCT_INVALID_PRICE)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "재고가 0 미만인 경우 (PRODUCT_INVALID_STOCK)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "재고가 최대치를 초과하는 경우 (PRODUCT_STOCK_OVERFLOW)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @PostMapping
    fun createProduct(
        @Parameter(description = "상품 생성 요청 정보", required = true)
        @RequestBody request: ProductRequest.Create
    ): BaseResponse<ProductResponse.Single>

    /**
     * 상품 재고를 업데이트합니다.
     *
     * @param productId 상품 ID
     * @param request 재고 업데이트 요청
     * @return 업데이트된 상품 정보
     */
    @Operation(
        summary = "상품 재고 업데이트",
        description = "상품의 재고를 특정 값으로 업데이트합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "재고 업데이트 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/ProductResponse.Single")
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품 ID가 빈 값인 경우 (PRODUCT_INVALID_ID)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품을 찾을 수 없는 경우 (PRODUCT_NOT_FOUND)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "재고가 0 미만인 경우 (PRODUCT_INVALID_STOCK)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "재고가 최대치를 초과하는 경우 (PRODUCT_STOCK_OVERFLOW)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @PutMapping("/{productId}/stock")
    fun updateStock(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable productId: String,
        @Parameter(description = "재고 업데이트 요청 정보", required = true)
        @RequestBody request: ProductRequest.UpdateStock
    ): BaseResponse<ProductResponse.Single>

    /**
     * 상품 재고를 증가시킵니다.
     *
     * @param productId 상품 ID
     * @param request 재고 증가 요청
     * @return 증가 후 상품 정보
     */
    @Operation(
        summary = "상품 재고 증가",
        description = "상품의 재고를 지정된 수량만큼 증가시킵니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "재고 증가 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/ProductResponse.Single")
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품 ID가 빈 값인 경우 (PRODUCT_INVALID_ID)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품을 찾을 수 없는 경우 (PRODUCT_NOT_FOUND)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "증가량이 0 이하인 경우 (PRODUCT_INVALID_INC_STOCK)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "증가 후 재고가 최대치를 초과하는 경우 (PRODUCT_STOCK_OVERFLOW)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @PostMapping("/{productId}/stock/increase")
    fun increaseStock(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable productId: String,
        @Parameter(description = "재고 증가 요청 정보", required = true)
        @RequestBody request: ProductRequest.IncreaseStock
    ): BaseResponse<ProductResponse.Single>

    /**
     * 상품 재고를 감소시킵니다.
     *
     * @param productId 상품 ID
     * @param request 재고 감소 요청
     * @return 감소 후 상품 정보
     */
    @Operation(
        summary = "상품 재고 감소",
        description = "상품의 재고를 지정된 수량만큼 감소시킵니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "재고 감소 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class, ref = "#/components/schemas/ProductResponse.Single")
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품 ID가 빈 값인 경우 (PRODUCT_INVALID_ID)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "상품을 찾을 수 없는 경우 (PRODUCT_NOT_FOUND)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "감소량이 0 이하인 경우 (PRODUCT_INVALID_DEC_STOCK)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "감소 후 재고가 0 미만인 경우 (PRODUCT_STOCK_UNDERFLOW)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Any::class)
                )]
            )
        ]
    )
    @PostMapping("/{productId}/stock/decrease")
    fun decreaseStock(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable productId: String,
        @Parameter(description = "재고 감소 요청 정보", required = true)
        @RequestBody request: ProductRequest.DecreaseStock
    ): BaseResponse<ProductResponse.Single>
} 