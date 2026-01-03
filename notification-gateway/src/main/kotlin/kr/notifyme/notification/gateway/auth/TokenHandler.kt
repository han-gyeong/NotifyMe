package kr.notifyme.notification.gateway.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import kr.notifyme.notification.gateway.config.JwtProperties
import org.springframework.stereotype.Component
import java.security.Key

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

    fun parseToken(token: String): Claims {
        return jwtParser.parseClaimsJws(token).body
    }

    fun getUserInfo(claims: Claims): String {
        return claims.subject
    }
}