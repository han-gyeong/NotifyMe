package kr.notifyme.notification.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotificationGatewayApplication

fun main(args: Array<String>) {
	runApplication<NotificationGatewayApplication>(*args)
}
