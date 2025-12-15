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
}