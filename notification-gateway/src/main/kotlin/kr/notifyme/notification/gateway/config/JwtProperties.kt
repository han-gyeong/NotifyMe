package kr.notifyme.notification.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("auth.jwt")
data class JwtProperties(
    var secret: String = ""
)
