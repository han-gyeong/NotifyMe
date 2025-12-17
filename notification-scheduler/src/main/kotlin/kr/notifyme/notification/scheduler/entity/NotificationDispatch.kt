package kr.notifyme.notification.scheduler.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class NotificationDispatch(

    @Id
    val notificationId: Long = 0,

    val lockedBy: String? = null,

    val lockToken: String? = null,

    val lockedUntil: LocalDateTime? = null
)