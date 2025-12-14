package kr.notifyme.notification.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.repository.NotificationRepository
import kr.notifyme.notification.support.OffsetLimit
import org.springframework.data.domain.Slice
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

    @Transactional
    fun modifyNotification(userId: String, notificationId: Long, request: ModifyNotificationRequest): Notification {
        val found = notificationRepository.findByCreatedByAndId(userId, notificationId)
            ?: throw IllegalArgumentException("No notification with id $notificationId found")

        require(found.status == NotificationStatus.WAITING) { "Cannot modify notification with id $notificationId" }

        found.modify(message = request.message, notifyAt = request.notifyAt)

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

        return found
    }
}