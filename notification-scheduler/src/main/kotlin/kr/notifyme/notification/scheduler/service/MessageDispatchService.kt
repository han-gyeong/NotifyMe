package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.scheduler.dto.SendRequest
import kr.notifyme.notification.scheduler.repository.NotificationRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import kotlin.jvm.javaClass

@Service
class MessageDispatchService(
    private val kafkaTemplate: KafkaTemplate<String, SendRequest>,
    private val notificationRepository: NotificationRepository,
    private val messageDispatchCommandService: MessageDispatchCommandService
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun doDispatch(lockToken: String) {
        val claims = messageDispatchCommandService.claimDispatch(lockToken)
        if (claims.isEmpty()) {
            return
        }

        val notifications = notificationRepository.findAllById(claims.map { dispatch -> dispatch.notificationId })
        notifications.forEach { notification ->
            val request = SendRequest(
                notificationId = notification.id,
                channelType = notification.channelType,
                destination = notification.destination,
                message = notification.message
            )

            kafkaTemplate.send("req", notification.id.toString(), request)
                .whenComplete { result, exception ->
                    if (exception == null) {
                        messageDispatchCommandService.markEnqueued(notificationId = notification.id)
                    } else {
                        log.error("Failed to send notification to ${notification.id}", exception)
                        messageDispatchCommandService.markFailed(notificationId = notification.id)
                    }
                }
        }
    }
}