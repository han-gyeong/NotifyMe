package kr.notifyme.notification.sender.service

import kotlinx.coroutines.test.runTest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.sender.dto.SendRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SlackChannelSenderTest(
    @Value("\${slack.webhook-url}") private val webhookUrl: String,
    @Autowired val slackChannelSender: SlackChannelSender
) {

    @Test
    fun `정상적으로 발송이 수행된다`() = runTest {
        // given
        val request = SendRequest(
            1L,
            ChannelType.SLACK,
            webhookUrl,
            "Hello, it's me"
        )

        // when
        val result = slackChannelSender.send(request)

        // then
        Assertions.assertThat(result.success).isTrue
    }

    @Test
    fun `이상한 URL일 경우 success 값은 False이다`() = runTest {
        // given
        val request = SendRequest(
            1L,
            ChannelType.SLACK,
            "https://hooks.slack.com/services/This-is-not-valid-address",
            "Hello, it's not me"
        )

        // when
        val result = slackChannelSender.send(request)

        // then
        Assertions.assertThat(result.success).isFalse
    }

    @Test
    fun `canHandle은 SLACK 타입에 대해 true를 반환한다`() {
        // given
        val request = SendRequest(
            1L,
            ChannelType.SLACK,
            webhookUrl,
            "message"
        )

        // when
        val result = slackChannelSender.canHandle(request)

        // then
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `canHandle은 SLACK 이외의 타입에 대해 false를 반환한다`() {
        // given
        val request = SendRequest(
            1L,
            ChannelType.EMAIL,
            webhookUrl,
            "message"
        )

        // when
        val result = slackChannelSender.canHandle(request)

        // then
        Assertions.assertThat(result).isFalse
    }
}