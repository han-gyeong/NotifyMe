package kr.notifyme.notification.auth.controller.v1

import kr.notifyme.notification.auth.support.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException::class)
    suspend fun handleRuntimeException(ex: RuntimeException): ApiResponse<Any> {
        return ApiResponse.error(ex.message ?: "")
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    suspend fun handleCommonException(ex: Exception): ApiResponse<Any> {
        return ApiResponse.error("Internal System Error")
    }
}