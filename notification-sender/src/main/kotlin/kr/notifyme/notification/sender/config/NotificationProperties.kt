package kr.notifyme.notification.sender.config

import kr.notifyme.notification.domain.ChannelType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "notification")
data class NotificationProperties(
    var channels: Map<ChannelType, ChannelConfig> = mutableMapOf()
)
