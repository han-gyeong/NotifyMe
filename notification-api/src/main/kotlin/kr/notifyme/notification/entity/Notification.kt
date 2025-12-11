package kr.notifyme.notification.entity

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import java.time.LocalDateTime

class Notification(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = "",

    @Enumerated(EnumType.STRING)
    var channelType: ChannelType,

    var message: String,

    var notifyAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: NotificationStatus,

    var createdBy: String = ""

) {
    fun canCancel(): Boolean = status == NotificationStatus.WAITING

    fun cancel() {
        status = NotificationStatus.CANCELLED
    }


}