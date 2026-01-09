package kr.notifyme.notification.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.event.EventType
import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.event.NotificationMessage
import kr.notifyme.notification.repository.NotificationRepository
import kr.notifyme.notification.support.OffsetLimit
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun scheduleNotification(userId: String, notificationRequest: NotificationRequest): Notification {
        val notification = notificationRepository.save(
            Notification(
                channelType = notificationRequest.channel,
                destination = notificationRequest.destination,
                message = notificationRequest.message,
                notifyAt = notificationRequest.notifyAt,
                status = NotificationStatus.WAITING,
                createdBy = userId
            )
        )

        publishEvent(EventType.CREATE, notification)

        return notification
    }

    @Transactional
    fun modifyNotification(userId: String, notificationId: Long, request: ModifyNotificationRequest): Notification {
        val found = notificationRepository.findByCreatedByAndId(userId, notificationId)
            ?: throw IllegalArgumentException("No notification with id $notificationId found")

        require(found.canModify()) { "Cannot modify notification with id $notificationId" }

        found.modify(message = request.message, notifyAt = request.notifyAt)

        publishEvent(EventType.MODIFY, found)

        return found
    }

    fun getNotificationById(userId: String, notificationId: Long): Notification {
        val found = notificationRepository.findByCreatedByAndId(userId, notificationId)
            ?: throw IllegalArgumentException("No notification with that id $notificationId found")

        return found
    }

    fun getAllNotificationByUserId(userId: String, offsetLimit: OffsetLimit): Slice<Notification> {
        return notificationRepository.findAllByCreatedBy(createdBy = userId, pageable = offsetLimit.toPageable())
    }

    @Transactional
    fun cancelNotification(userId: String, notificationId: Long): Notification {
        val found = notificationRepository.findByCreatedByAndId(userId, notificationId)
            ?: throw IllegalArgumentException("No notification with id $notificationId found")

        require(found.canCancel()) { "Cannot cancel notification with id $notificationId" }

        found.cancel()

        publishEvent(EventType.CANCEL, found)

        return found
    }

    private fun publishEvent(eventType: EventType, notification: Notification) {
        eventPublisher.publishEvent(NotificationEvent(
            id = UUID.randomUUID().toString(),
            aggregateId = notification.id,
            operationType = eventType,
            payload = NotificationMessage.fromNotification(notification),
            createdAt = LocalDateTime.now(),
        ))
    }
}