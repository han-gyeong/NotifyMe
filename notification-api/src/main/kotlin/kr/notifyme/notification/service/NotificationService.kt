package kr.notifyme.notification.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.controller.v1.response.NotificationResponse
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.repository.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {

    @Transactional
    fun scheduleNotification(userId: String, notificationRequest: NotificationRequest): Notification {
        val notification = notificationRepository.save(
            Notification(
                channelType = notificationRequest.channel,
                message = notificationRequest.message,
                notifyAt = notificationRequest.notifyAt,
                status = NotificationStatus.WAITING,
                createdBy = userId
            )
        )

        return notification
    }

    fun getNotificationById(userId: String, notificationId: String): Notification {
        val found = notificationRepository.findByCreatedByAndId(userId, notificationId)
            ?: throw IllegalArgumentException("No notification with that id $notificationId found")

        return found
    }

    fun getAllNotificationByUserId(userId: String): List<Notification> {
        return notificationRepository.findAllByCreatedBy(createdBy = userId)
    }

    @Transactional
    fun cancelNotification(userId: String, notificationId: String): Notification {
        return notificationRepository.findByCreatedByAndId(userId, notificationId)
            ?.takeIf { it.canCancel() }
            ?.also { it.cancel() }
            ?: throw IllegalArgumentException("No notification with that id $notificationId found")
    }
}