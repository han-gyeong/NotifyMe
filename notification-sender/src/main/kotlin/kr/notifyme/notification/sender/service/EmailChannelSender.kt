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
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
@ConditionalOnProperty(name = ["sender.type"], havingValue = "email")
class EmailChannelSender(
    private val mailProperties: MailProperties,
    private val dnsResolver: DnsResolver
) : ChannelSender {

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
                request(mxRecord, mailProperties.port, request.destination, "NotifyMe 에서 알람을 보내드립니다!", request.message)
                isSuccess = true
                break
            } catch (temporary: SmtpTemporaryException) {
                errorMsg = temporary.message ?: "temporary error occurred in sending email"
                continue
            } catch (permanent: SmtpPermanentException) {
                errorMsg = permanent.message ?: "permanent error occurred in sending email"
                break
            } catch (socket: SocketTimeoutException) {
                errorMsg = socket.message ?: "socket timeout"
                continue
            } catch (ioe: IOException) {
                errorMsg = ioe.message ?: "error occurred in sending email"
                continue
            } catch (e: Exception) {
                errorMsg = e.message ?: "unexpected error occurred in sending email"
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
        socket.connect(InetSocketAddress(host, port), convertSecondsIntoMillis(mailProperties.connectTimeout))
        socket.soTimeout = convertSecondsIntoMillis(mailProperties.readTimeout)

        socket.use { socket ->
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            fun readAndCheck(): String {
                val lines = StringBuilder()
                while (true) {
                    val line = reader.readLine() ?: throw SmtpTemporaryException("Got unexpected response")
                    lines.append(line)
                        .append(System.lineSeparator())

                    if (line.length < 4 || line[3] == ' ') {
                        break
                    }
                }

                val line = lines.toString()
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
            send("Date: ${ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)}")
            send("Message-ID: <${UUID.randomUUID()}@notifyme.kr>")
            send("MIME-Version: 1.0")
            send("Content-Type: text/plain; charset=UTF-8")
            send("Content-Transfer-Encoding: 8bit")
            send("")

            normalizeMessage(message).lines().forEach {
                send(it)
            }

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

    private fun normalizeMessage(message: String): String {
        return message.replace("\r\n", "\n")
            .replace("\r", "\n")
            .lines()
            .joinToString("\r\n") {
                line -> if (line.startsWith(".")) ".$line" else line
            }
    }

    private fun convertSecondsIntoMillis(seconds: Int): Int {
        return seconds * 1000
    }
}