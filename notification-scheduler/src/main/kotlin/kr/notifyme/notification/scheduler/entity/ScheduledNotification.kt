package kr.notifyme.notification.scheduler.entity

import jakarta.persistence.*
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import java.time.LocalDateTime

@Entity
class ScheduledNotification(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val notificationId: Long,

    @Enumerated(EnumType.STRING)
    val channelType: ChannelType,

    val destination: String,

    var message: String,

    var notifyAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: NotificationStatus,

    val createdBy: String = "",

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val modifiedAt: LocalDateTime = LocalDateTime.now(),
) {

    fun modify(message: String, notifyAt: LocalDateTime) {
        this.message = message
        this.notifyAt = notifyAt
    }

    fun cancel() {
        this.status = NotificationStatus.CANCELLED
    }
}