package kr.notifyme.notification.auth.controller.v1

import kr.notifyme.notification.auth.controller.v1.request.LoginRequest
import kr.notifyme.notification.auth.controller.v1.request.SignUpRequest
import kr.notifyme.notification.auth.controller.v1.response.LoginResponse
import kr.notifyme.notification.auth.service.AuthService
import kr.notifyme.notification.auth.support.response.ApiResponse
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import java.time.Duration

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/signup")
    suspend fun signUp(@RequestBody request: SignUpRequest): ApiResponse<Any> {
        authService.signUp(request)

        return ApiResponse.success()
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest, exchange: ServerWebExchange): ApiResponse<LoginResponse> {
        val tokenResponse = authService.login(request)

        setRefreshTokenCookie(tokenResponse.refreshToken, exchange)

        return ApiResponse.success(LoginResponse(accessToken = tokenResponse.accessToken))
    }

    @PostMapping("/refresh")
    suspend fun refresh(@CookieValue("refresh_token") refreshToken: String, exchange: ServerWebExchange): Any {
        val tokenResponse = authService.refresh(refreshToken)

        setRefreshTokenCookie(tokenResponse.refreshToken, exchange)

        return ApiResponse.success(LoginResponse(accessToken = tokenResponse.accessToken))
    }

    @PostMapping("/logout")
    suspend fun logout(@RequestHeader("Authorization") authHeader: String, exchange: ServerWebExchange): ApiResponse<Any> {
        val token = authHeader.removePrefix("Bearer").trim()
        authService.logout(token)

        expireRefreshTokenCookie(exchange)

        return ApiResponse.success()
    }

    private fun setRefreshTokenCookie(refreshToken: String, exchange: ServerWebExchange) {
        val refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(Duration.ofDays(7))
            .sameSite("Lax")
            .build();

        exchange.response.addCookie(refreshCookie)
    }

    private fun expireRefreshTokenCookie(exchange: ServerWebExchange) {
        val refreshCookie = ResponseCookie.from("refresh_token", "")
            .path("/")
            .maxAge(0)
            .build()

        exchange.response.addCookie(refreshCookie)
    }
}