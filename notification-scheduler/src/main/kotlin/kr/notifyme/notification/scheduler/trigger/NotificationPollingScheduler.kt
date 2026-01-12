package kr.notifyme.notification.scheduler.trigger

import kr.notifyme.notification.scheduler.service.NotificationDeliveryService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationPollingScheduler(
    val notificationDeliveryService: NotificationDeliveryService,
) {

    @Scheduled(fixedDelay = 1000)
    fun execute() {
        notificationDeliveryService.doDeliver()
    }

}