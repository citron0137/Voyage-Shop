package kr.hhplus.be.server.controller.product

import kr.hhplus.be.server.controller.product.request.CreateProductRequest
import kr.hhplus.be.server.controller.product.request.UpdateProductStockRequest
import kr.hhplus.be.server.controller.product.response.ProductResponseDTO
import kr.hhplus.be.server.controller.shared.BaseResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController()
class ProductController {

    @PostMapping("/products")
    fun createProduct(
        @RequestBody req: CreateProductRequest
    ): BaseResponse<ProductResponseDTO>{
        return BaseResponse.success(ProductResponseDTO(
            id = "id1",
            name = req.name,
            price = req.price,
            stock = req.stock,
        ))
    }

    @GetMapping("/products")
    fun getAllProducts(): BaseResponse<List<ProductResponseDTO>>{
        return BaseResponse.success(listOf(
            ProductResponseDTO(
                id = "id1",
                name = UUID.randomUUID().toString(),
                price = 0,
                stock = 0,
            ),
            ProductResponseDTO(
                id = "id2",
                name = UUID.randomUUID().toString(),
                price = 0,
                stock = 0,
            ),
        ) )
    }

    @GetMapping("/products/{id}")
    fun getOneProduct(
        @PathVariable id: String
    ): BaseResponse<ProductResponseDTO>{
        return BaseResponse.success(ProductResponseDTO(
            id = "id1",
            name = UUID.randomUUID().toString(),
            price = 0,
            stock = 0,
        ))
    }

    @PutMapping("/products/{id}/stock")
    fun updateProductStock(
        @PathVariable id: String,
        @RequestBody req: UpdateProductStockRequest
    ): BaseResponse<ProductResponseDTO>{
        return BaseResponse.success(
            ProductResponseDTO(
                id = "id1",
                name = UUID.randomUUID().toString(),
                price = 0,
                stock = req.stock,
            ),
        )
    }
}