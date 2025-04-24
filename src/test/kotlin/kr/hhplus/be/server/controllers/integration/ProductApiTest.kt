package kr.hhplus.be.server.controllers.integration

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.controller.product.ProductRequest
import kr.hhplus.be.server.controller.shared.BaseResponse
import kr.hhplus.be.server.domain.product.ProductRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@DisplayName("상품 API 테스트")
@Transactional
class ProductApiTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @Autowired private lateinit var objectMapper: ObjectMapper

    @Autowired private lateinit var productRepository: ProductRepository

    private var testProductId: String = ""

    @BeforeEach
    fun setUp() {
        // 테스트용 상품 생성
        val createRequest = ProductRequest.Create(name = "테스트 상품", price = 10000L, stock = 100L)

        val createResponse =
                mockMvc.perform(
                                post("/api/v1/products")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest))
                        )
                        .andReturn()

        val responseContent = createResponse.response.contentAsString
        val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
        val dataMap = responseObj.data as Map<*, *>
        testProductId = dataMap["productId"] as String
    }

    @Nested
    @DisplayName("상품 목록 조회 시")
    inner class ProductListRetrieval {

        @Test
        @DisplayName("모든 상품 목록이 반환되어야 한다")
        fun returnsAllProducts() {
            // when
            val result =
                    mockMvc.perform(get("/api/v1/products").contentType(MediaType.APPLICATION_JSON))

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.products").isArray)
                    .andExpect(jsonPath("$.data.products.length()").isNumber)
                    .andExpect(jsonPath("$.data.products[0].productId").exists())
                    .andExpect(jsonPath("$.data.products[0].name").exists())
                    .andExpect(jsonPath("$.data.products[0].price").exists())
                    .andExpect(jsonPath("$.data.products[0].stock").exists())
        }
    }

    @Nested
    @DisplayName("개별 상품 조회 시")
    inner class ProductRetrieval {

        @Test
        @DisplayName("유효한 상품 ID로 조회하면 상품 정보가 반환되어야 한다")
        fun withValidId_returnsProduct() {
            // when
            val result =
                    mockMvc.perform(
                            get("/api/v1/products/{productId}", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.productId").value(testProductId))
                    .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                    .andExpect(jsonPath("$.data.price").value(10000))
                    .andExpect(jsonPath("$.data.stock").value(100))
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회하면 에러가 반환되어야 한다")
        fun withNonExistentId_returnsError() {
            // when
            val result =
                    mockMvc.perform(
                            get("/api/v1/products/{productId}", "non-existent-id")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"))
        }
    }

    @Nested
    @DisplayName("상품 생성 시")
    inner class ProductCreation {

        @Test
        @DisplayName("유효한 정보로 요청하면 상품이 생성되어야 한다")
        fun withValidRequest_createsProduct() {
            // given
            val createRequest = ProductRequest.Create(name = "새 상품", price = 5000L, stock = 50L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.productId").exists())
                    .andExpect(jsonPath("$.data.name").value("새 상품"))
                    .andExpect(jsonPath("$.data.price").value(5000))
                    .andExpect(jsonPath("$.data.stock").value(50))
        }

        @Test
        @DisplayName("상품명이 빈 값인 경우 에러가 반환되어야 한다")
        fun withBlankName_returnsError() {
            // given
            val createRequest = ProductRequest.Create(name = "", price = 5000L, stock = 50L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_INVALID_NAME"))
        }

        @Test
        @DisplayName("가격이 0 이하인 경우 에러가 반환되어야 한다")
        fun withInvalidPrice_returnsError() {
            // given
            val createRequest = ProductRequest.Create(name = "가격 오류 상품", price = 0L, stock = 50L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_INVALID_PRICE"))
        }

        @Test
        @DisplayName("재고가 0 미만인 경우 에러가 반환되어야 한다")
        fun withNegativeStock_returnsError() {
            // given
            val createRequest = ProductRequest.Create(name = "재고 오류 상품", price = 5000L, stock = -1L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_INVALID_STOCK"))
        }
    }

    @Nested
    @DisplayName("상품 재고 업데이트 시")
    inner class ProductStockUpdate {

        @Test
        @DisplayName("유효한 재고 값으로 업데이트하면 성공해야 한다")
        fun withValidStock_updatesStock() {
            // given
            val updateRequest = ProductRequest.UpdateStock(stock = 200L)

            // when
            val result =
                    mockMvc.perform(
                            put("/api/v1/products/{productId}/stock", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.productId").value(testProductId))
                    .andExpect(jsonPath("$.data.stock").value(200))
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 업데이트하면 에러가 반환되어야 한다")
        fun withNonExistentId_returnsError() {
            // given
            val updateRequest = ProductRequest.UpdateStock(stock = 200L)

            // when
            val result =
                    mockMvc.perform(
                            put("/api/v1/products/{productId}/stock", "non-existent-id")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"))
        }

        @Test
        @DisplayName("재고가 0 미만인 경우 에러가 반환되어야 한다")
        fun withNegativeStock_returnsError() {
            // given
            val updateRequest = ProductRequest.UpdateStock(stock = -1L)

            // when
            val result =
                    mockMvc.perform(
                            put("/api/v1/products/{productId}/stock", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_INVALID_STOCK"))
        }
    }

    @Nested
    @DisplayName("상품 재고 증가 시")
    inner class ProductStockIncrease {

        @Test
        @DisplayName("유효한 증가량으로 요청하면 재고가 증가해야 한다")
        fun withValidAmount_increasesStock() {
            // given
            val increaseRequest = ProductRequest.IncreaseStock(amount = 50L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products/{productId}/stock/increase", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(increaseRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.productId").value(testProductId))
                    .andExpect(jsonPath("$.data.stock").value(150)) // 초기값 100 + 증가량 50
        }

        @Test
        @DisplayName("증가량이 0 이하인 경우 에러가 반환되어야 한다")
        fun withInvalidAmount_returnsError() {
            // given
            val increaseRequest = ProductRequest.IncreaseStock(amount = 0L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products/{productId}/stock/increase", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(increaseRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_INVALID_INC_STOCK"))
        }
    }

    @Nested
    @DisplayName("상품 재고 감소 시")
    inner class ProductStockDecrease {

        @Test
        @DisplayName("유효한 감소량으로 요청하면 재고가 감소해야 한다")
        fun withValidAmount_decreasesStock() {
            // given
            val decreaseRequest = ProductRequest.DecreaseStock(amount = 30L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products/{productId}/stock/decrease", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(decreaseRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.productId").value(testProductId))
                    .andExpect(jsonPath("$.data.stock").value(70)) // 초기값 100 - 감소량 30
        }

        @Test
        @DisplayName("감소량이 0 이하인 경우 에러가 반환되어야 한다")
        fun withInvalidAmount_returnsError() {
            // given
            val decreaseRequest = ProductRequest.DecreaseStock(amount = 0L)

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products/{productId}/stock/decrease", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(decreaseRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_INVALID_DEC_STOCK"))
        }

        @Test
        @DisplayName("재고보다 큰 감소량으로 요청하면 에러가 반환되어야 한다")
        fun withExcessiveAmount_returnsError() {
            // given
            val decreaseRequest = ProductRequest.DecreaseStock(amount = 101L) // 초기 재고(100)보다 큰 값

            // when
            val result =
                    mockMvc.perform(
                            post("/api/v1/products/{productId}/stock/decrease", testProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(decreaseRequest))
                    )

            // then
            result.andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_STOCK_UNDERFLOW"))
        }
    }

    @Nested
    @DisplayName("E2E 테스트 시나리오")
    inner class E2EScenario {

        @Test
        @DisplayName("상품 생성 및 재고 관리 흐름이 정상 동작해야 한다")
        fun createAndManageProduct_succeedsEndToEnd() {
            // 1. 상품 생성
            val createRequest =
                    ProductRequest.Create(name = "E2E 테스트 상품", price = 15000L, stock = 100L)

            val createResponse =
                    mockMvc.perform(
                                    post("/api/v1/products")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(createRequest))
                            )
                            .andExpect(status().isOk)
                            .andExpect(jsonPath("$.success").value(true))
                            .andReturn()

            val responseContent = createResponse.response.contentAsString
            val responseObj = objectMapper.readValue(responseContent, BaseResponse::class.java)
            val dataMap = responseObj.data as Map<*, *>
            val createdProductId = dataMap["productId"] as String
            val initialStock = dataMap["stock"] as Int

            // 2. 재고 증가
            val increaseRequest = ProductRequest.IncreaseStock(amount = 50L)
            val increaseResponse =
                    mockMvc.perform(
                                    post(
                                                    "/api/v1/products/{productId}/stock/increase",
                                                    createdProductId
                                            )
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(increaseRequest)
                                            )
                            )
                            .andExpect(status().isOk)
                            .andExpect(jsonPath("$.success").value(true))
                            .andExpect(jsonPath("$.data.stock").value(initialStock + 50))
                            .andReturn()

            val increaseResponseContent = increaseResponse.response.contentAsString
            val increaseResponseObj =
                    objectMapper.readValue(increaseResponseContent, BaseResponse::class.java)
            val increaseDataMap = increaseResponseObj.data as Map<*, *>
            val afterIncreaseStock = increaseDataMap["stock"] as Int

            // 3. 재고 감소
            val decreaseRequest = ProductRequest.DecreaseStock(amount = 30L)
            mockMvc.perform(
                            post("/api/v1/products/{productId}/stock/decrease", createdProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(decreaseRequest))
                    )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stock").value(afterIncreaseStock - 30))

            // 4. 최종 상품 조회
            mockMvc.perform(
                            get("/api/v1/products/{productId}", createdProductId)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.productId").value(createdProductId))
                    .andExpect(jsonPath("$.data.name").value("E2E 테스트 상품"))
                    .andExpect(jsonPath("$.data.price").value(15000))
                    .andExpect(jsonPath("$.data.stock").value(afterIncreaseStock - 30))
        }
    }
}
