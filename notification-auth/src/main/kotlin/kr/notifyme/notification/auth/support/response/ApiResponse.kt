package kr.notifyme.notification.auth.support.response

import kr.notifyme.notification.auth.support.ResultType

data class ApiResponse<T> private constructor(
    var result: ResultType,
    var message: String,
    var data: T? = null
) {
    companion object {
        fun success(): ApiResponse<Any> {
            return ApiResponse(ResultType.SUCCESS, "Success", null)
        }

        fun <T> success(data: T?) : ApiResponse<T> {
            return ApiResponse(ResultType.SUCCESS, "Success", data)
        }

        fun error(message: String): ApiResponse<Any> {
            return ApiResponse(ResultType.ERROR, message, null)
        }
    }
}