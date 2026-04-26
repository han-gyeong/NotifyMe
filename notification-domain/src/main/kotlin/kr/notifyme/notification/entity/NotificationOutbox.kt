package kr.notifyme.notification.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_notification_outbox_01", columnList = "status, next_publish_at, notification_id")
    ]
)
class NotificationOutbox(

    @Id
    val notificationId: Long,

    @Column(length = 4000)
    val payload: String,

    @Enumerated(EnumType.STRING)
    var status: OutboxStatus,

    val createdAt: LocalDateTime,

    var retryCount: Int = 0,

    var nextPublishAt: LocalDateTime? = null,
)