package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.entity.OutboxStatus
import kr.notifyme.notification.scheduler.repository.NotificationDispatchRepository
import kr.notifyme.notification.scheduler.repository.NotificationOutboxRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationOutboxService(
    private val notificationOutboxRepository: NotificationOutboxRepository,
    private val notificationDispatchRepository: NotificationDispatchRepository,
) {

    @Transactional
    fun findReadyOutboxes(maxRetryCount: Int, size: Int): List<NotificationOutbox> {
        return notificationOutboxRepository.findReadyOutboxes(
            OutboxStatus.WAITING,
            LocalDateTime.now(),
            maxRetryCount,
            PageRequest.ofSize(size)
        )
    }

    @Transactional
    fun markSent(outbox: NotificationOutbox) {
        notificationOutboxRepository.updateStatus(outbox.notificationId, OutboxStatus.SENT)
        notificationDispatchRepository.updateStatus(outbox.notificationId, NotificationStatus.SENT)
    }

    @Transactional
    fun markFailed(outbox: NotificationOutbox) {
        notificationOutboxRepository.updateStatus(outbox.notificationId, OutboxStatus.FAILED)
        notificationDispatchRepository.updateStatus(outbox.notificationId, NotificationStatus.FAILED)
    }

    @Transactional
    fun updateNextRetry(outbox: NotificationOutbox, nextRetryAt: LocalDateTime) {
        notificationOutboxRepository.updateNextRetry(outbox.notificationId, nextRetryAt)
    }
}
