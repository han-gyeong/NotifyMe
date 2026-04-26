package kr.notifyme.notification.scheduler.scheduler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.scheduler.config.NotificationProperties
import kr.notifyme.notification.scheduler.dto.SendRequest
import kr.notifyme.notification.scheduler.service.NotificationOutboxService
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class NotificationOutboxPublisher(
    private val notificationOutboxService: NotificationOutboxService,
    private val kafkaTemplate: KafkaTemplate<String, SendRequest>,
    private val notificationProperties: NotificationProperties,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotificationOutboxPublisher::class.java)
        private const val MAX_RETRY_COUNT = 3
        private const val BATCH_SIZE = 100
    }

    @Transactional
    @Scheduled(fixedDelay = 1000)
    fun publishOutbox() {
        val outboxes = notificationOutboxService.findReadyOutboxes(MAX_RETRY_COUNT, BATCH_SIZE)

        for (outbox in outboxes) {
            publish(outbox)
        }
    }

    private fun publish(outbox: NotificationOutbox) {
        try {
            val request = objectMapper.readValue(outbox.payload, SendRequest::class.java)
            val topic = notificationProperties.channels[request.channelType]?.topicRequest
                ?: throw IllegalArgumentException("Unknown channel type: ${request.channelType}")

            kafkaTemplate.send(topic, outbox.notificationId.toString(), request).get()
            notificationOutboxService.markSent(outbox)
            log.info("Published dispatch request. notificationId={}", outbox.notificationId)
        } catch (e: Exception) {
            handlePublishFailure(outbox, e)
        }
    }

    private fun handlePublishFailure(outbox: NotificationOutbox, exception: Exception) {
        if (exception is JsonProcessingException || exception is IllegalArgumentException) {
            notificationOutboxService.markFailed(outbox)
            log.error("Failed to publish dispatch request permanently. notificationId={}", outbox.notificationId, exception)
            return
        }

        if (outbox.retryCount >= MAX_RETRY_COUNT) {
            notificationOutboxService.markFailed(outbox)
            log.error("Failed to publish dispatch request after retries. notificationId={}", outbox.notificationId, exception)
            return
        }

        notificationOutboxService.updateNextRetry(outbox,calculateNextRetryAt(outbox.retryCount + 1))
        log.warn("Failed to publish dispatch request. notificationId={}, retryCount={}",
            outbox.notificationId,
            outbox.retryCount + 1,
            exception
        )
    }

    private fun calculateNextRetryAt(retryCount: Int): LocalDateTime {
        return LocalDateTime.now().plusMinutes(retryCount.toLong() + 1)
    }
}
