package kr.notifyme.notification.sender.service

import kotlinx.coroutines.test.runTest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.sender.dto.SendRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class SlackChannelSenderTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var slackChannelSender: SlackChannelSender

    @BeforeEach
    fun generateMockWeb() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val webClientBuilder = WebClient.builder()
        slackChannelSender = SlackChannelSender(webClientBuilder)
    }

    @Test
    fun `정상적으로 발송이 수행된다`() = runTest {
        // given
        val webhookUrl = mockWebServer.url("/valid-hook").toString()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))

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

        val body = mockWebServer.takeRequest().body.readUtf8()
        Assertions.assertThat(body).contains(request.message)
    }

    @Test
    fun `이상한 URL일 경우 success 값은 False이다`() = runTest {
        // given
        val webhookUrl = mockWebServer.url("/invalid-hook").toString()
        mockWebServer.enqueue(MockResponse().setResponseCode(403).setBody("invalid_token"))

        val request = SendRequest(
            1L,
            ChannelType.SLACK,
            webhookUrl,
            "Hello, it's not me"
        )

        // when
        val result = slackChannelSender.send(request)

        // then
        Assertions.assertThat(result.success).isFalse

        val body = mockWebServer.takeRequest().body.readUtf8()
        Assertions.assertThat(body).contains(request.message)
    }

    @Test
    fun `canHandle은 SLACK 타입에 대해 true를 반환한다`() {
        // given
        val request = SendRequest(
            1L,
            ChannelType.SLACK,
            "",
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
            "",
            "message"
        )

        // when
        val result = slackChannelSender.canHandle(request)

        // then
        Assertions.assertThat(result).isFalse
    }
}