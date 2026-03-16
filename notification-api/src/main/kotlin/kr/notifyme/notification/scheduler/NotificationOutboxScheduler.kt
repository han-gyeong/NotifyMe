package kr.notifyme.notification.scheduler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import kr.notifyme.notification.entity.OutboxStatus
import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.repository.NotificationOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class NotificationOutboxScheduler(
    private val notificationOutboxRepository: NotificationOutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, NotificationEvent>,
    private val objectMapper: ObjectMapper,
    @Value("\${notification.topics.event}") private val eventTopic: String
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotificationOutboxScheduler::class.java)
        private const val MAX_RETRY_COUNT = 3
    }

    @Transactional
    @Scheduled(fixedDelay = 1000)
    fun retryPublish() {
        val now = LocalDateTime.now()
        val events = notificationOutboxRepository.findEventForRetry(
            OutboxStatus.WAITING,
            now,
            MAX_RETRY_COUNT,
            PageRequest.ofSize(100)
        )

        for (event in events) {
            try {
                val payload = convertFromJson(event.payload)

                publishEvent(payload)
                updateToSent(event.eventId)
            } catch (e: Exception) {
                when (e) {
                    is JsonProcessingException -> updateToFail(event.eventId)
                    else -> {
                        val retryCount = event.retryCount + 1
                        updateNextRetry(event.eventId, retryCount, calculateNextRetryAt(retryCount))
                    }
                }
            }
        }
    }

    fun publishEvent(event: NotificationEvent): SendResult<String?, NotificationEvent?>? {
        return kafkaTemplate.send(eventTopic, event.notificationId.toString(), event).get()
    }

    fun updateToFail(eventId: String) {
        notificationOutboxRepository.updateOutboxStatus(eventId, OutboxStatus.FAILED)
    }

    fun updateToSent(eventId: String) {
        notificationOutboxRepository.updateOutboxStatus(eventId, OutboxStatus.SENT)
    }

    fun updateNextRetry(eventId: String, retryCount: Int, nextRetryAt: LocalDateTime) {
        notificationOutboxRepository.updateNextRetry(eventId, retryCount, nextRetryAt)
    }

    fun calculateNextRetryAt(retryCount: Int): LocalDateTime {
        return LocalDateTime.now().plusMinutes(retryCount.toLong() + 1)
    }

    private fun convertFromJson(json: String): NotificationEvent = objectMapper.readValue(json, NotificationEvent::class.java)
}