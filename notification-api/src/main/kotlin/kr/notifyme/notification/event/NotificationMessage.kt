package kr.notifyme.notification.event

import com.fasterxml.jackson.annotation.JsonFormat
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.entity.Notification
import java.time.LocalDateTime

data class NotificationMessage(
    val id: Long,
    val channel: ChannelType,
    val destination: String,
    val message: String,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val notifyAt: LocalDateTime
) {
    companion object {
        fun fromNotification(notification: Notification): NotificationMessage {
            return NotificationMessage(
                id = notification.id,
                channel = notification.channelType,
                destination = notification.destination,
                message = notification.message,
                notifyAt = notification.notifyAt
            )
        }
    }
}
