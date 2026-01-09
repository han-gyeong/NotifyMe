package kr.notifyme.notification.event

import java.time.LocalDateTime

data class NotificationEvent(

    val id: String,

    val aggregateId: Long,

    val operationType: EventType,

    val payload: NotificationMessage,

    val createdAt: LocalDateTime,

    )
