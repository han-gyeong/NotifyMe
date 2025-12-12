package kr.notifyme.notification.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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

    val message: String,

    val notifyAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: NotificationStatus,

    val createdBy: String

) {
    fun canCancel(): Boolean = status == NotificationStatus.WAITING

    fun cancel() {
        status = NotificationStatus.CANCELLED
    }


}