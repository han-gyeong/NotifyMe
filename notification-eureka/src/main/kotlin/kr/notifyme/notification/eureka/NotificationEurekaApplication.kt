package kr.notifyme.notification.eureka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
class NotificationEurekaApplication

fun main(args: Array<String>) {
	runApplication<NotificationEurekaApplication>(*args)
}
