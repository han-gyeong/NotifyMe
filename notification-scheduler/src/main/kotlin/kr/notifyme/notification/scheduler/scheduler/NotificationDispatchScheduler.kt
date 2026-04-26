package kr.notifyme.notification.scheduler.scheduler

import kr.notifyme.notification.scheduler.service.NotificationDispatchService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationDispatchScheduler(
    private val notificationDispatchService: NotificationDispatchService,
){

    companion object {
        private val log = LoggerFactory.getLogger(NotificationDispatchScheduler::class.java)
    }

    @Scheduled(fixedDelay = 1000)
    fun dispatchToOutbox() {
        val dispatched = notificationDispatchService.dispatchToOutbox();
        log.info("Message Dispatched To Outbox : {}", dispatched)
    }
}