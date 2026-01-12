package kr.notifyme.notification.event

import java.time.LocalDateTime

data class NotificationEvent(

    val eventId: String,

    val notificationId: Long,

    val eventType: EventType,

    val payload: NotificationMessage,

    val createdAt: LocalDateTime,
    )
