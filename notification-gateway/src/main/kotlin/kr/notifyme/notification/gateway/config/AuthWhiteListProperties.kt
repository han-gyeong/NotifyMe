package kr.notifyme.notification.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth.whitelist")
data class AuthWhiteListProperties(
    var paths: List<String> = mutableListOf()
)
