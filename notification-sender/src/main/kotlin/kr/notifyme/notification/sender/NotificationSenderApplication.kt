package kr.notifyme.notification.sender

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotificationSenderApplication

fun main(args: Array<String>) {
	runApplication<NotificationSenderApplication>(*args)
}
