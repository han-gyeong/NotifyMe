package kr.notifyme.notification.event

import kr.notifyme.notification.domain.ChannelType
import java.time.LocalDateTime

data class NotificationMessage(
    val id: Long,
    val channel: ChannelType,
    val destination: String,
    val message: String,
    val notifyAt: LocalDateTime
)
