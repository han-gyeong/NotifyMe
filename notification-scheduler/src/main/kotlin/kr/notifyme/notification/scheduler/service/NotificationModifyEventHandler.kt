package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.event.EventType
import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.scheduler.repository.ScheduledNotificationRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationModifyEventHandler(
    private val scheduledNotificationRepository: ScheduledNotificationRepository
): NotificationEventHandler {

    override fun canHandle(event: NotificationEvent) = event.eventType == EventType.MODIFY

    @Transactional
    override fun handle(event: NotificationEvent) {
        val foundNotification = scheduledNotificationRepository.findByNotificationId(event.notificationId)
            ?: throw IllegalArgumentException("Scheduled notification not found: $event")

        val request = event.payload
        foundNotification.modify(request.message, request.notifyAt)
    }
}