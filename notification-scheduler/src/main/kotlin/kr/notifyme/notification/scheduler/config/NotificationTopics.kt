package kr.notifyme.notification.scheduler.config

import kr.notifyme.notification.domain.ChannelType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "notification.topics")
data class NotificationTopics(
    var request: Map<ChannelType, String> = emptyMap(),
    var result: Map<ChannelType, String> = emptyMap()
)