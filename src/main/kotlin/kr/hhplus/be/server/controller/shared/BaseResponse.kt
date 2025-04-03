package kr.hhplus.be.server.controller.shared

class BaseResponseErrorDTO (
    val code: String,
    val message: String,
)
class BaseResponse<T> (
    val success: Boolean,
    val data: T? = null,
    val error: BaseResponseErrorDTO? = null,
){
    companion object {
        fun <T> success(data: T): BaseResponse<T> {
            return BaseResponse(success = true, data = data)
        }
        fun <T> fail(code: String, message: String): BaseResponse<T> {
            return BaseResponse(
                success = false,
                error = BaseResponseErrorDTO(code, message)
            )
        }
    }
}