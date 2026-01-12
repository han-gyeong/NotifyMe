package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.scheduler.config.NotificationProperties
import kr.notifyme.notification.scheduler.dto.SendRequest
import kr.notifyme.notification.scheduler.repository.ScheduledNotificationRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class MessageDispatchService(
    private val kafkaTemplate: KafkaTemplate<String, SendRequest>,
    private val notificationProperties: NotificationProperties,
    private val scheduledNotificationRepository: ScheduledNotificationRepository) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun doDispatch() {
        val readyNotifications =
            scheduledNotificationRepository.findReadyNotificationsWithLock(LocalDateTime.now(), Pageable.ofSize(100))

        readyNotifications.forEach { notification ->
            val request = SendRequest(
                notificationId = notification.notificationId,
                channelType = notification.channelType,
                destination = notification.destination,
                message = notification.message
            )

            val channelProps = notificationProperties.channels[notification.channelType] ?: run {
                log.error("Received message dispatch request for unknown channel type: ${notification.channelType}")
                updateStatus(notification.notificationId, NotificationStatus.FAILED)
                return@forEach
            }

            try {
                kafkaTemplate.send(channelProps.topicRequest, notification.id.toString(), request)
                    .get(3, TimeUnit.SECONDS)

                updateStatus(notification.notificationId, NotificationStatus.SENT)
            } catch (e: Exception) {
                log.error("Failed to send notification to ${notification.notificationId}", e)
                updateStatus(notification.notificationId, NotificationStatus.FAILED)
            }
        }
    }

    fun updateStatus(notificationId: Long, status: NotificationStatus) {
        scheduledNotificationRepository.updateStatus(notificationId, status)
    }
}