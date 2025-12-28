package kr.notifyme.notification.sender.listener

import kotlinx.coroutines.runBlocking
import kr.notifyme.notification.sender.config.NotificationProperties
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
    private val senders: List<ChannelSender>,
    private val notificationProperties: NotificationProperties
) {

    @KafkaListener(
        topics = ["#{notificationProperties.channels['\${sender.type}'].topicRequest}"],
        groupId = "#{notificationProperties.channels['\${sender.type}'].groupId}"
    )
    @SendTo("#{notificationProperties.channels['\${sender.type}'].topicResult}")
    fun onMessage(request: SendRequest): SendResult = runBlocking{
        try {
            val sender = senders.find { sender -> sender.canHandle(request) }
                ?: throw IllegalArgumentException("No Sender found for $request")

            sender.send(request)
        } catch (e: Exception) {
            SendResult(
                notificationId = request.notificationId,
                channelType = request.channelType,
                success = false,
                errorCode = "99",
                errorMessage = e.message ?: "error")
        }

    }

}