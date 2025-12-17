package kr.notifyme.notification.controller.v1.response

import com.fasterxml.jackson.annotation.JsonFormat
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val channel: ChannelType,
    val destination: String,
    val message: String,
    val status: NotificationStatus,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val notifyAt: LocalDateTime
) {
    companion object {
        fun of(notification: Notification): NotificationResponse {
            return NotificationResponse(
                id = notification.id,
                channel = notification.channelType,
                destination = notification.destination,
                message = notification.message,
                status = notification.status,
                notifyAt = notification.notifyAt,
            )
        }
    }
}
