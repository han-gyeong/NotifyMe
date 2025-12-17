package kr.notifyme.notification.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class NotificationSchedulerApplication

fun main(args: Array<String>) {
    runApplication<NotificationSchedulerApplication>(*args)
}