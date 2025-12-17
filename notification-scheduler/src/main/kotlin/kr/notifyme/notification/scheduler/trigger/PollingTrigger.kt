package kr.notifyme.notification.scheduler.trigger

import kr.notifyme.notification.scheduler.service.MessageDispatchService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PollingTrigger(
    val messageDispatchService: MessageDispatchService,
) {

    @Scheduled(fixedDelay = 5000)
    fun execute() {
        messageDispatchService.doDispatch(UUID.randomUUID().toString())
    }

}