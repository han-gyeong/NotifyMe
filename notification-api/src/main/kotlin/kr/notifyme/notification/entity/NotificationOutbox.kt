package kr.notifyme.notification.entity

import jakarta.persistence.*
import kr.notifyme.notification.event.EventType
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_notification_outbox_01", columnList = "status, next_retry_at")
    ]
)
class NotificationOutbox(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val outboxId: Long = 0,

    @Column(unique = true, nullable = false)
    val eventId: String,

    val notificationId: Long,

    @Enumerated(EnumType.STRING)
    val eventType: EventType,

    @Column(length = 4000)
    val payload: String,

    @Enumerated(EnumType.STRING)
    var status: OutboxStatus,

    val createdAt: LocalDateTime,

    var retryCount: Int = 0,

    var nextRetryAt: LocalDateTime? = null,
    )