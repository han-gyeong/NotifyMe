package kr.notifyme.notification.gateway.auth

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.getAndAwait
import org.springframework.stereotype.Component

@Component
class TokenCacheService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
) {

    suspend fun isTokenBlacklisted(accessToken: String): Boolean {
        val key = "BL:$accessToken"
        return reactiveRedisTemplate.opsForValue().getAndAwait(key) != null
    }
}
