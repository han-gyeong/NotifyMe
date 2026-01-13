package kr.notifyme.notification.scheduler.trigger

import kr.notifyme.notification.scheduler.service.NotificationWorker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationPollingScheduler(
    val notificationWorker: NotificationWorker,
) {

    @Scheduled(fixedDelay = 1000)
    fun execute() {
        notificationWorker.work()
    }

}