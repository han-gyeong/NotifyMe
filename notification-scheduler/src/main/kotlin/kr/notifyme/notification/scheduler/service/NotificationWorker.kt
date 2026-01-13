package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.scheduler.config.NotificationProperties
import kr.notifyme.notification.scheduler.dto.SendRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class NotificationWorker(
    private val kafkaTemplate: KafkaTemplate<String, SendRequest>,
    private val notificationProperties: NotificationProperties,
    private val scheduledNotificationService: ScheduledNotificationService,
    ) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun work() {
        val readySchedule = scheduledNotificationService.lockAndMarkProcessing()

        readySchedule.forEach { schedule ->
            val request = SendRequest(
                notificationId = schedule.notificationId,
                channelType = schedule.channelType,
                destination = schedule.destination,
                message = schedule.message
            )

            val channelProps = notificationProperties.channels[schedule.channelType] ?: run {
                log.error("Received message dispatch request for unknown channel type: ${schedule.channelType}")
                scheduledNotificationService.updateStatus(schedule.id, NotificationStatus.FAILED)
                return@forEach
            }

            kafkaTemplate.send(channelProps.topicRequest, schedule.id.toString(), request)
                .whenComplete { _, exception ->
                    if (exception == null) {
                        scheduledNotificationService.updateStatus(schedule.id, NotificationStatus.SENT)
                        log.info("Sent message, scheduleId: ${schedule.id}, notificationId: ${schedule.notificationId}")
                    } else {
                        log.error("Failed to send message, scheduleId: ${schedule.id}, notificationId: ${schedule.notificationId}", exception)
                        scheduledNotificationService.updateStatus(schedule.id, NotificationStatus.FAILED)
                    }
                }
        }
    }
}