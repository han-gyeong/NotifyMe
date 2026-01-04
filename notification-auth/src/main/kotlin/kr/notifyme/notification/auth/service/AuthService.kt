package kr.notifyme.notification.auth.service

import kr.notifyme.notification.auth.controller.v1.request.LoginRequest
import kr.notifyme.notification.auth.controller.v1.request.SignUpRequest
import kr.notifyme.notification.auth.dto.TokenResponse
import kr.notifyme.notification.auth.entity.User
import kr.notifyme.notification.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val tokenHandler: TokenHandler,
    private val tokenCacheService: TokenCacheService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun signUp(request: SignUpRequest): User {
        if (userRepository.existsByUserId(request.userId)) {
            throw RuntimeException("User already exists")
        }

        if (userRepository.existsByEmail(request.email)) {
            throw RuntimeException("Email already exists")
        }

        val user = User(
            userId = request.userId,
            password = passwordEncoder.encode(request.password),
            email = request.email
        )

        return userRepository.save(user)
    }

    suspend fun login(request: LoginRequest): TokenResponse {
        val found = userRepository.findByUserId(request.userId)
            ?: throw RuntimeException("Invalid ID or password")

        if (!passwordEncoder.matches(request.password, found.password)) {
            throw RuntimeException("Invalid ID or password")
        }

        val accessToken = tokenHandler.createAccessToken(found.userId)
        val refreshToken = tokenHandler.createRefreshToken(found.userId)

        tokenCacheService.saveRefreshToken(found.userId, refreshToken)

        return TokenResponse(accessToken, refreshToken)
    }

    suspend fun logout(accessToken: String) {
        try {
            val claims = tokenHandler.parseToken(accessToken)
            val userId = claims.subject

            tokenCacheService.removeRefreshToken(userId)

            val remainingTime = tokenHandler.getRemainingExpiration(accessToken)
            tokenCacheService.addBlacklist(accessToken, remainingTime)

        } catch (e: Exception) {
            throw RuntimeException("Invalid Token", e)
        }
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        val claims = try {
            tokenHandler.parseToken(refreshToken)
        } catch (e: Exception) {
            throw RuntimeException("Invalid Token", e)
        }

        val userId = claims.subject

        val cachedToken = tokenCacheService.getRefreshToken(userId)
            ?: throw RuntimeException("Login session has expired")

        if (cachedToken != refreshToken) {
            tokenCacheService.removeRefreshToken(userId)
            throw RuntimeException("Unmatched refresh token, Please login again")
        }

        val newAccessToken = tokenHandler.createAccessToken(userId)
        val newRefreshToken = tokenHandler.createRefreshToken(userId)
        tokenCacheService.saveRefreshToken(userId, newRefreshToken)

        return TokenResponse(newAccessToken, newRefreshToken)
    }
}