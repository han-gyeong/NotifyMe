package kr.notifyme.notification.controller

import jakarta.validation.ConstraintViolationException
import kr.notifyme.notification.support.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException) : ApiResponse<Any> {
        val violated = e.constraintViolations.joinToString("\n") { it -> "${it.propertyPath.last().name}: ${it.message}" }

        return ApiResponse.error(violated)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException) : ApiResponse<Any> {
        val message = e.message ?: ""
        return ApiResponse.error(message)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    suspend fun handleCommonException(ex: Exception): ApiResponse<Any> {
        return ApiResponse.error("Internal System Error")
    }
}