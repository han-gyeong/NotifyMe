package kr.notifyme.notification.sender.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.sender.config.MailProperties
import kr.notifyme.notification.sender.dto.SendRequest
import kr.notifyme.notification.sender.dto.SendResult
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailChannelSender(
    private val mailSender: JavaMailSender,
    private val mailProperties: MailProperties
) : ChannelSender {

    override fun canHandle(request: SendRequest): Boolean = request.channelType == ChannelType.EMAIL

    override suspend fun send(request: SendRequest): SendResult = withContext(Dispatchers.IO) {
        try {
            val message = mailSender.createMimeMessage();
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(mailProperties.from)
            helper.setTo(request.destination)
            helper.setSubject(request.message.substring(0, 10))
            helper.setText(request.message)

            mailSender.send(message)

            SendResult(
                notificationId = request.notificationId,
                channelType = request.channelType,
                success = true,
                errorCode = "",
                errorMessage = "",
            )
        } catch (e: Exception) {
            println(e.printStackTrace())

            SendResult(
                notificationId = request.notificationId,
                channelType = request.channelType,
                success = false,
                errorCode = "500",
                errorMessage = e.message ?: "Unknown error",
            )
        }
    }
}