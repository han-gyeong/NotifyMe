package kr.notifyme.notification.entity

import jakarta.persistence.*
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import java.time.LocalDateTime

@Entity
class Notification(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    val channelType: ChannelType,

    val destination: String,

    var message: String,

    var notifyAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: NotificationStatus,

    val createdBy: String

) {
    fun canCancel(): Boolean = status == NotificationStatus.WAITING

    fun cancel() {
        status = NotificationStatus.CANCELLED
    }

    fun modify(message: String, notifyAt: LocalDateTime) {
        this.message = message
        this.notifyAt = notifyAt
    }

    fun changeStatus(status: NotificationStatus) {
        this.status = status
    }
}