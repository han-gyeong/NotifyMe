package kr.notifyme.notification.sender.listener

import kr.notifyme.notification.sender.dto.SendRequest
import kr.notifyme.notification.sender.dto.SendResult
import kr.notifyme.notification.sender.service.ChannelSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["sender.type"])
class SendRequestListener(
    private val senders: List<ChannelSender>
) {

    @KafkaListener(
        topics = ["#{notificationProperties.channels['\${sender.type}'].topicRequest}"],
        groupId = "#{notificationProperties.channels['\${sender.type}'].groupId}",
        concurrency = "#{notificationProperties.channels['\${sender.type}'].concurrency}",
    )
    @SendTo("#{notificationProperties.channels['\${sender.type}'].topicResult}")
    suspend fun onMessage(request: SendRequest): SendResult {
        try {
            val sender = senders.find { it.canHandle(request) }
                ?: throw IllegalArgumentException("No Sender found for $request")

            return sender.send(request)
        } catch (e: Exception) {
            return SendResult(
                notificationId = request.notificationId,
                channelType = request.channelType,
                success = false,
                errorCode = "99",
                errorMessage = e.message ?: "error")
        }
    }
}