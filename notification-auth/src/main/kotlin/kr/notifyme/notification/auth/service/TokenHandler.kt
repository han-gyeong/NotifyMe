package kr.notifyme.notification.auth.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kr.notifyme.notification.auth.config.JwtProperties
import org.springframework.stereotype.Component
import java.security.Key
import java.time.Instant
import java.util.*

@Component
class TokenHandler(
    private val jwtProperties: JwtProperties
) {

    private val signingKey: Key by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    private val jwtParser: JwtParser by lazy {
        Jwts.parserBuilder().setSigningKey(signingKey).build()
    }

    fun createAccessToken(userId: String): String {
        return createToken(userId, jwtProperties.accessTokenExpiration)
    }

    fun createRefreshToken(userId: String): String {
        return createToken(userId, jwtProperties.refreshTokenExpiration)
    }

    private fun createToken(userId: String, expirationInSeconds: Long): String {
        val now = Instant.now()
        val expireDate = Date.from(now.plusSeconds(expirationInSeconds))

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(Date.from(now))
            .setExpiration(expireDate)
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun parseToken(token: String): Claims {
        return jwtParser.parseClaimsJws(token).body
    }

    fun getRemainingExpiration(token: String): Long {
        val expirationTime = parseToken(token).expiration.time
        val currentTime = System.currentTimeMillis()

        val diff = currentTime - expirationTime

        return if (diff > 0) (diff / 1000) else 0
    }
}