package kr.notifyme.notification.consumer

import kr.notifyme.notification.consumer.dto.SendResult
import kr.notifyme.notification.service.NotificationResultService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SendResultListener(
    private val notificationResultService: NotificationResultService
) {

    @KafkaListener(
        topics = ["\${notification.topics.result}"],
        groupId = "\${notification.consumer.group-id}",
        concurrency = "\${notification.consumer.concurrency}",
    )
    fun onMessage(result: SendResult) {
        notificationResultService.processResult(result)
    }
}