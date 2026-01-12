package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.event.EventType
import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.scheduler.entity.ScheduledNotification
import kr.notifyme.notification.scheduler.repository.ScheduledNotificationRepository
import org.springframework.stereotype.Component

@Component
class NotificationRegistEventHandler(
    private val scheduledNotificationRepository: ScheduledNotificationRepository
): NotificationEventHandler {

    override fun canHandle(event: NotificationEvent) = event.eventType == EventType.CREATE

    override fun handle(event: NotificationEvent) {
        val message = event.payload

        val notification = ScheduledNotification(
            notificationId = event.notificationId,
            channelType = message.channel,
            destination = message.destination,
            message = message.message,
            notifyAt = message.notifyAt,
            status = NotificationStatus.WAITING
        )

        scheduledNotificationRepository.save(notification)
    }
}