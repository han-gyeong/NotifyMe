package kr.notifyme.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class NotificationApiApplication

fun main(args: Array<String>) {
	runApplication<NotificationApiApplication>(*args)
}
