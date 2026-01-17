package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.scheduler.config.ChannelConfig
import kr.notifyme.notification.scheduler.config.NotificationProperties
import kr.notifyme.notification.scheduler.dto.SendRequest
import kr.notifyme.notification.scheduler.entity.ScheduledNotification
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

        readySchedule.forEach { schedule -> processSchedule(schedule) }
    }

    private fun processSchedule(schedule: ScheduledNotification) {
        val channelConfig = validateChannel(schedule) ?: return
        val request = createSendRequest(schedule)

        sendNotification(schedule.id, request, channelConfig)
    }

    private fun createSendRequest(schedule: ScheduledNotification): SendRequest = SendRequest(
        notificationId = schedule.notificationId,
        channelType = schedule.channelType,
        destination = schedule.destination,
        message = schedule.message
    )

    private fun validateChannel(schedule: ScheduledNotification): ChannelConfig? {
        return notificationProperties.channels[schedule.channelType] ?: run {
            log.error("Received message dispatch request for unknown channel type: ${schedule.channelType}")
            updateStatusToFailed(schedule.id)
            null
        }
    }

    private fun sendNotification(scheduleId: Long, request: SendRequest, channelConfig: ChannelConfig) {
        kafkaTemplate.send(channelConfig.topicRequest, scheduleId.toString(), request)
            .whenComplete { _, exception ->
                handleSendResult(scheduleId, exception)
            }
    }

    private fun handleSendResult(scheduleId: Long, exception: Throwable?) {
        if (exception == null) {
            updateStatusToSent(scheduleId)
            log.info("Sent message, scheduleId: $scheduleId")
        } else {
            updateStatusToFailed(scheduleId)
            log.error("Failed to send message, scheduleId: $scheduleId", exception)
        }
    }

    private fun updateStatusToSent(scheduleId: Long) {
        scheduledNotificationService.updateStatus(scheduleId, NotificationStatus.SENT)
    }

    private fun updateStatusToFailed(scheduleId: Long) {
        scheduledNotificationService.updateStatus(scheduleId, NotificationStatus.FAILED)
    }
}