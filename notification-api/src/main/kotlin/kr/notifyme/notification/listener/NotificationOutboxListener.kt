package kr.notifyme.notification.listener

import com.fasterxml.jackson.databind.ObjectMapper
import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.entity.OutboxStatus
import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.service.NotificationOutboxService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class NotificationOutboxListener(
    private val notificationOutboxService: NotificationOutboxService,
    private val kafkaTemplate: KafkaTemplate<String, NotificationEvent>,
    private val objectMapper: ObjectMapper,
    @Value("\${notification.topics.event}") private val eventTopic: String
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotificationOutboxListener::class.java)
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveOutbox(event: NotificationEvent) {
        notificationOutboxService.save(
            NotificationOutbox(
                eventId = event.eventId,
                eventType = event.eventType,
                notificationId = event.notificationId,
                payload = convertToJson(event),
                status = OutboxStatus.WAITING,
                createdAt = event.createdAt,
            )
        )
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishRealtime(event: NotificationEvent) {
        val foundEvent = notificationOutboxService.findByEventId(event.eventId)
            ?: throw IllegalArgumentException("No notification outbox found with id ${event.eventId}")

        kafkaTemplate.send(eventTopic, foundEvent.notificationId.toString(), event)
            .whenComplete { _, ex ->
                if (ex == null) {
                    log.info("Published notification outbox with id ${foundEvent.notificationId}")
                    notificationOutboxService.markSent(foundEvent.outboxId)
                } else {
                    log.error("Failed to publish notification", ex)
                    notificationOutboxService.markFailed(foundEvent.outboxId)
                }
            }
    }

    private fun convertToJson(message: Any): String = objectMapper.writeValueAsString(message)
}