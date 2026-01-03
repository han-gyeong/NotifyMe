package kr.notifyme.notification.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.consumer.dto.SendResult
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationResultService(
    private val notificationRepository: NotificationRepository,
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotificationResultService::class.java)
    }

    @Transactional
    fun processResult(result: SendResult) {
        val found = notificationRepository.findById(result.notificationId)
            .orElseThrow { throw IllegalArgumentException("No notification with that id ${result.notificationId}") }

        log.info("[Handle Result] ID: {}, Success: {}", found.id, result.success)

        found.changeStatus(if (result.success) NotificationStatus.SUCCESS else NotificationStatus.FAILED)
    }

}