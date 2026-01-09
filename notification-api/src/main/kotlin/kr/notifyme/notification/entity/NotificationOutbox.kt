package kr.notifyme.notification.entity

import jakarta.persistence.*
import kr.notifyme.notification.event.EventType
import java.time.LocalDateTime

@Entity
class NotificationOutbox(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val outboxId: Long = 0,

    @Column(unique = true, nullable = false)
    val eventId: String,

    val aggregateId: Long,

    @Enumerated(EnumType.STRING)
    val eventType: EventType,

    val payload: String,

    @Enumerated(EnumType.STRING)
    var status: OutboxStatus,

    val createdAt: LocalDateTime

    )