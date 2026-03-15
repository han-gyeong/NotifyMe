package kr.notifyme.notification.sender.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "mail")
data class MailProperties(
    var from: String = "webmaster@notifyme.kr",
    var host: String = "localhost",
    var port: Int = 25,
    var username: String = "user",
    var password: String = "password",
    var heloHost: String = "mail.notifyme.kr",
    val mailFrom: String = "alert@notifyme.kr",
)