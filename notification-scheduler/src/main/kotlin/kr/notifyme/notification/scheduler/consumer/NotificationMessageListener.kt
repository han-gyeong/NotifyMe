package kr.notifyme.notification.scheduler.consumer

import kr.notifyme.notification.event.NotificationEvent
import kr.notifyme.notification.scheduler.service.NotificationEventHandler
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationMessageListener(
    private val eventHandlers: List<NotificationEventHandler>
) {

    @KafkaListener(
        topics = ["notification-topic"],
        groupId = "notification-scheduler",
        concurrency = "5"
    )
    fun onMessage(event: NotificationEvent) {
        eventHandlers.find { it.canHandle(event) }
            ?.handle(event)
            ?: log.error("Notification event handler not found: $event")

        log.info("Handling Event : $event")
    }

    companion object {
        private val log = LoggerFactory.getLogger(NotificationMessageListener::class.java)
    }
}