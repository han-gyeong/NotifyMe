package kr.notifyme.notification.sender.service

import kotlinx.coroutines.test.runTest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.sender.dto.SendRequest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class EmailChannelSenderTest(
    @Autowired val emailChannelSender: EmailChannelSender
) {

    @Test
    fun `정상적으로 발송이 수행된다`() = runTest {
        // given
        val request = SendRequest(
            1L,
            ChannelType.EMAIL,
            "yixel63618@roratu.com",
            "Hello, it's not me"
        )

        val result = emailChannelSender.send(request)

        assertThat(result.notificationId).isEqualTo(request.notificationId)
        assertThat(result.channelType).isEqualTo(request.channelType)
        assertThat(result.success).isTrue
    }
}