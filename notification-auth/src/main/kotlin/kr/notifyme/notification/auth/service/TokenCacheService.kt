package kr.notifyme.notification.auth.service

import kr.notifyme.notification.auth.config.JwtProperties
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.deleteAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setAndAwait
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TokenCacheService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val jwtProperties: JwtProperties
) {

    suspend fun getRefreshToken(userId: String): String? {
        val key = "RT:$userId"
        return reactiveRedisTemplate.opsForValue().getAndAwait(key)
    }

    suspend fun saveRefreshToken(userId: String, refreshToken: String): Boolean {
        val key = "RT:$userId"
        return reactiveRedisTemplate.opsForValue().setAndAwait(key, refreshToken, Duration.ofSeconds(jwtProperties.refreshTokenExpiration))
    }

    suspend fun removeRefreshToken(userId: String): Boolean {
        val key = "RT:$userId"
        return reactiveRedisTemplate.deleteAndAwait(key) == 1L
    }

    suspend fun addBlacklist(accessToken: String, expirationInSeconds: Long): Boolean {
        val key = "BL:$accessToken"
        return reactiveRedisTemplate.opsForValue().setAndAwait(accessToken, key, Duration.ofSeconds(expirationInSeconds))
    }
}