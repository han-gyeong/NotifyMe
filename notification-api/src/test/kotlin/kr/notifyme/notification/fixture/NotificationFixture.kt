package kr.notifyme.notification.fixture

import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import java.time.LocalDateTime
import java.util.*

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

        fun createNotification(
            id: Long = 1L,
            channelType: ChannelType = ChannelType.EMAIL,
            destination: String = "test@test.com",
            message: String = "TEST1234",
            notifyAt: LocalDateTime = LocalDateTime.now(),
            status: NotificationStatus = NotificationStatus.WAITING,
            createdBy: String = "TEST-USER-1"
        ): Notification {
            return Notification(
                id = id,
                channelType = channelType,
                destination = destination,
                message = message,
                notifyAt = notifyAt,
                status = status,
                createdBy = createdBy
            )
        }
    }

}