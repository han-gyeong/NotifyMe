package kr.notifyme.notification.scheduler.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.event.EventType
import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.scheduler.repository.ScheduledNotificationRepository
import org.springframework.stereotype.Component

@Component
class NotificationCancelEventHandler(
    private val scheduledNotificationRepository: ScheduledNotificationRepository
): NotificationEventHandler {

    override fun canHandle(event: NotificationEvent) = event.eventType == EventType.CANCEL

    @Transactional
    override fun handle(event: NotificationEvent) {
        val foundNotification = scheduledNotificationRepository.findByNotificationId(event.notificationId)
            ?: throw IllegalArgumentException("Scheduled notification not found: $event")

        foundNotification.cancel()
    }
}