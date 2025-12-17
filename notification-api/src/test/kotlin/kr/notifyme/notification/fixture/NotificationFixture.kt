package kr.notifyme.notification.fixture

import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import java.time.LocalDateTime
import java.util.UUID

class NotificationFixture {

    companion object {
        fun createNotification(userId: String): Notification = Notification(
            channelType = ChannelType.EMAIL,
            destination = "hello@example.com",
            message = "TEST" + UUID.randomUUID().toString(),
            createdBy = userId,
            notifyAt = LocalDateTime.now(),
            status = NotificationStatus.WAITING
        )
    }

}