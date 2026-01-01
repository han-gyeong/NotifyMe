package kr.notifyme.notification.sender.service

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import kotlinx.coroutines.test.runTest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.sender.config.MailConfig
import kr.notifyme.notification.sender.config.MailProperties
import kr.notifyme.notification.sender.dto.SendRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [EmailChannelSender::class, MailProperties::class, MailConfig::class])
@EnableConfigurationProperties(MailProperties::class)
@TestPropertySource(properties = ["sender.type=email"])
class EmailChannelSenderTest(
    @Autowired val emailChannelSender: EmailChannelSender
) {

    companion object {
        @JvmField
        @RegisterExtension
        val greenMail = GreenMailExtension(
            ServerSetup(3025, "localhost", ServerSetup.PROTOCOL_SMTP)
        ).withConfiguration(
            GreenMailConfiguration.aConfig()
                .withUser("notifyme@test.com", "test1234")
        )
    }

    @Test
    fun `정상적으로 발송이 수행된다`() = runTest {
        // given
        val request = SendRequest(
            1L,
            ChannelType.EMAIL,
            "yixel63618@roratu.com",
            "Hello, it's not me"
        )

        // when
        val result = emailChannelSender.send(request)

        // then
        // 결과 수신 검증
        assertThat(result.notificationId).isEqualTo(request.notificationId)
        assertThat(result.channelType).isEqualTo(request.channelType)
        assertThat(result.success).isTrue

        // GreenMail 검증
        assertThat(greenMail.waitForIncomingEmail(1)).isTrue
        val receivedMessages = greenMail.receivedMessages

        assertThat(receivedMessages).hasSize(1)

        val message = receivedMessages[0]
        assertThat(message.allRecipients).hasSize(1)
        assertThat(message.allRecipients[0].toString()).isEqualTo(request.destination)
        assertThat(GreenMailUtil.getBody(message)).contains(request.message)
    }

    @Test
    fun `canHandle은 EMAIL 타입에 대해 true를 반환한다`() {
        // given
        val request = SendRequest(
            1L,
            ChannelType.EMAIL,
            "test@test.com",
            "message"
        )

        // when
        val result = emailChannelSender.canHandle(request)

        // then
        assertThat(result).isTrue
    }

    @Test
    fun `canHandle은 EMAIL 이외의 타입에 대해 false를 반환한다`() {
        // given
        val request = SendRequest(
            1L,
            ChannelType.SLACK,
            "test@test.com",
            "message"
        )

        // when
        val result = emailChannelSender.canHandle(request)

        // then
        assertThat(result).isFalse
    }
}