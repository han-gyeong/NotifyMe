package kr.notifyme.notification.sender.service

import kotlinx.coroutines.reactor.awaitSingle
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.sender.dto.SendRequest
import kr.notifyme.notification.sender.dto.SendResult
import kr.notifyme.notification.sender.service.request.SlackWebhookRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SlackChannelSender(
    webClientBuilder: WebClient.Builder
) : ChannelSender {

    private val webClient = webClientBuilder.build()

    override fun canHandle(request: SendRequest): Boolean =
        request.channelType == ChannelType.SLACK


    override suspend fun send(request: SendRequest): SendResult {
        return webClient.post()
            .uri(request.destination)
            .bodyValue(SlackWebhookRequest(request.message))
            .exchangeToMono { response ->
                if (response.statusCode().is2xxSuccessful) {
                    response.bodyToMono(Void::class.java)
                        .thenReturn(SendResult(request.notificationId, request.channelType, true, "", ""))
                } else {
                    response.bodyToMono(String::class.java)
                        .map { errorMsg ->
                            SendResult(
                                request.notificationId,
                                request.channelType,
                                false,
                                response.statusCode().toString(),
                                errorMsg
                            )
                        }
                }
            }
            .awaitSingle();
    }
}