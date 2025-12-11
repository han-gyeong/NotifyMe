package kr.notifyme.notification.controller.v1.response

import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.entity.Notification
import java.time.LocalDateTime

data class NotificationResponse(
    val id: String,
    val channel: ChannelType,
    val message: String,
    val notifyAt: LocalDateTime
) {
    companion object {
        fun of(notification: Notification): NotificationResponse {
            return NotificationResponse(
                id = notification.id,
                channel = notification.channelType,
                message = notification.message,
                notifyAt = notification.notifyAt
            )
        }
    }
}
