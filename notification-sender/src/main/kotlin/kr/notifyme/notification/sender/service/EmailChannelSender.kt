package kr.notifyme.notification.sender.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.sender.config.MailProperties
import kr.notifyme.notification.sender.dto.SendRequest
import kr.notifyme.notification.sender.dto.SendResult
import kr.notifyme.notification.sender.exception.SmtpPermanentException
import kr.notifyme.notification.sender.exception.SmtpTemporaryException
import kr.notifyme.notification.sender.service.util.DnsResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

@Component
@ConditionalOnProperty(name = ["sender.type"], havingValue = "email")
class EmailChannelSender(
    private val mailSender: JavaMailSender,
    private val mailProperties: MailProperties,
    private val dnsResolver: DnsResolver
) : ChannelSender {

    companion object {
        private const val SMTP_PORT = 25
        private const val CONNECT_TIMEOUT_MS = 5_000
        private const val READ_TIMEOUT_MS = 5_000
    }

    override fun canHandle(request: SendRequest): Boolean = request.channelType == ChannelType.EMAIL

    override suspend fun send(request: SendRequest): SendResult = withContext(Dispatchers.IO) {
        val domain = extractDomain(request.destination)
        val mxRecords = dnsResolver.resolveMx(domain)
        if (mxRecords.isEmpty()) {
            return@withContext SendResult(
                request.notificationId,
                ChannelType.EMAIL,
                false,
                "",
                "No MX records were found"
            )
        }

        var isSuccess = false
        var errorMsg = ""
        for (mxRecord in mxRecords) {
            try {
                request(mxRecord, SMTP_PORT, request.destination, "NotifyMe 에서 알람을 보내드립니다!", request.message)
                isSuccess = true
                break
            } catch (temporary: SmtpTemporaryException) {
                errorMsg = temporary.message ?: "temporary error occurred in sending email"
                continue
            } catch (permanent: SmtpPermanentException) {
                errorMsg = permanent.message ?: "permanent error occurred in sending email"
                break
            }
        }

        SendResult(
            request.notificationId,
            ChannelType.EMAIL,
            isSuccess,
            "",
            errorMsg
        )
    }

    private fun request(host: String, port: Int, destination: String, subject: String, message: String) {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
        socket.soTimeout = READ_TIMEOUT_MS

        socket.use { socket ->
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            fun readAndCheck(): String {
                val line = reader.readLine()
                println("S: $line")

                return when {
                    line.startsWith("2") -> line
                    line.startsWith("3") -> line
                    line.startsWith("4") -> throw SmtpTemporaryException(line)
                    else -> throw SmtpPermanentException(line)
                }
            }

            fun send(cmd: String) {
                println("C: $cmd")
                writer.write((cmd + "\r\n"))
                writer.flush()
            }

            // 서버 greeting
            readAndCheck()

            // HELO
            send("HELO ${mailProperties.heloHost}")
            readAndCheck()

            // MAIL FROM
            send("MAIL FROM:<${mailProperties.mailFrom}>")
            readAndCheck()

            // RCPT TO
            send("RCPT TO:<$destination>")
            readAndCheck()

            // DATA 시작
            send("DATA")
            readAndCheck()

            // 메일 헤더 + 바디
            send("From: <${mailProperties.mailFrom}>")
            send("To: <$destination>")
            send("Subject: ${encodeSubject(subject)}")
            send("Date: ${java.time.LocalDateTime.now()}")
            send("Message-ID: <${System.currentTimeMillis()}@notifyme.kr>")
            send("MIME-Version: 1.0")
            send("Content-Type: text/plain; charset=UTF-8")
            send("Content-Transfer-Encoding: 8bit")
            send("")
            send(message)
            send(".")

            writer.flush()

            readAndCheck()

            // 종료
            send("QUIT")
            readAndCheck()
        }
    }

    private fun extractDomain(email: String): String {
        return email.substring(email.indexOf('@') + 1)
    }

    private fun encodeSubject(subject: String): String {
        val encoded = Base64.getEncoder().encodeToString(subject.toByteArray(Charsets.UTF_8))

        return "=?UTF-8?B?$encoded?="
    }
}