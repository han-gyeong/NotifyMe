package kr.notifyme.notification.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("auth.jwt")
data class JwtProperties(
    var secret: String = "",
    val accessTokenExpiration: Long = 1800,
    val refreshTokenExpiration: Long = 604800,
)
