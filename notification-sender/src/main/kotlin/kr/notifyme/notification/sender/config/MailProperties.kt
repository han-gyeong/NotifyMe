package kr.notifyme.notification.sender.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "mail")
data class MailProperties(
    var port: Int = 25,
    var heloHost: String = "mail.notifyme.kr",
    val mailFrom: String = "alert@notifyme.kr",
    val connectTimeout: Int = 5,
    val readTimeout: Int = 5,
)