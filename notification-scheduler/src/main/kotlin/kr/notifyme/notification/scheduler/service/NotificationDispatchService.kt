package kr.notifyme.notification.scheduler.service

import com.fasterxml.jackson.databind.ObjectMapper
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.entity.OutboxStatus
import kr.notifyme.notification.scheduler.dto.SendRequest
import kr.notifyme.notification.scheduler.repository.NotificationDispatchRepository
import kr.notifyme.notification.scheduler.repository.NotificationOutboxRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationDispatchService(
    private val notificationDispatchRepository: NotificationDispatchRepository,
    private val notificationOutboxRepository: NotificationOutboxRepository,
    private val objectMapper: ObjectMapper,
    ) {

    @Transactional
    fun dispatchToOutbox(): Int {
        val readyNotifications = lockAndMarkProcessing();
        if (readyNotifications.isEmpty()) {
            return 0
        }

        notificationOutboxRepository.saveAll(readyNotifications.map { it.toOutbox() })

        return readyNotifications.size
    }

    fun lockAndMarkProcessing(): List<Notification> {
        val readySchedule = notificationDispatchRepository.findReadyNotificationsWithLock(
            NotificationStatus.WAITING,
            LocalDateTime.now(),
            Pageable.ofSize(100)
        )
        if (readySchedule.isEmpty()) {
            return emptyList()
        }

        notificationDispatchRepository.updateStatus(
            readySchedule.map { it.id },
            NotificationStatus.IN_PROGRESS
        )

        return readySchedule
    }

    private fun Notification.toOutbox(): NotificationOutbox {
        val request = SendRequest(
            notificationId = id,
            channelType = channelType,
            destination = destination,
            message = message
        )

        return NotificationOutbox(
            notificationId = id,
            payload = objectMapper.writeValueAsString(request),
            status = OutboxStatus.WAITING,
            createdAt = LocalDateTime.now(),
            nextPublishAt = LocalDateTime.now()
        )
    }
}