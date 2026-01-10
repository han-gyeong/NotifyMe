package kr.notifyme.notification.listener

import com.fasterxml.jackson.databind.ObjectMapper
import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.entity.OutboxStatus
import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.event.NotificationMessage
import kr.notifyme.notification.service.NotificationOutboxService
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.TimeUnit

@Component
class NotificationOutboxListener(
    private val notificationOutboxService: NotificationOutboxService,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
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
                aggregateId = event.notificationId,
                payload = convertToJson(event.payload),
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

        try {
            kafkaTemplate.send("notification-topic", foundEvent.aggregateId.toString(), foundEvent.payload)
                .get(3, TimeUnit.SECONDS)

            log.info("Published notification outbox with id ${foundEvent.aggregateId}, payload: ${foundEvent.payload}")

            notificationOutboxService.markSent(foundEvent.outboxId)
        } catch (e: Exception) {
            log.error("Failed to publish notification", e)

            notificationOutboxService.markFailed(foundEvent.outboxId)
        }
    }

    private fun convertToJson(message: NotificationMessage): String = objectMapper.writeValueAsString(message)
}