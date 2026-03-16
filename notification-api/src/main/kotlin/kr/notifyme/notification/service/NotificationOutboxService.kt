package kr.notifyme.notification.service

import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.entity.OutboxStatus
import kr.notifyme.notification.repository.NotificationOutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NotificationOutboxService(
    private val notificationOutboxRepository: NotificationOutboxRepository,
) {

    @Transactional(readOnly = true)
    fun findByEventId(eventId: String): NotificationOutbox? {
        return notificationOutboxRepository.findByEventId(eventId)
            ?: throw IllegalArgumentException("No notification outbox found with id ${eventId}")
    }

    fun save(outbox: NotificationOutbox) {
        notificationOutboxRepository.save(outbox)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markSent(eventId: String) {
        val foundOutbox = notificationOutboxRepository.findByEventId(eventId)
            ?: throw IllegalArgumentException("No notification outbox found with id $eventId")

        foundOutbox.status = OutboxStatus.SENT
    }
}