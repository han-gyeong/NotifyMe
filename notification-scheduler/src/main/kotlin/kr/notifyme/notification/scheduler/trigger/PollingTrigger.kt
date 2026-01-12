package kr.notifyme.notification.scheduler.trigger

import kr.notifyme.notification.scheduler.service.MessageDispatchService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PollingTrigger(
    val messageDispatchService: MessageDispatchService,
) {

    @Scheduled(fixedDelay = 1000)
    fun execute() {
        messageDispatchService.doDispatch()
    }

}