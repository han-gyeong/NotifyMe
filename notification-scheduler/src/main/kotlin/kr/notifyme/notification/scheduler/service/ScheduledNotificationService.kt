package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.scheduler.entity.ScheduledNotification
import kr.notifyme.notification.scheduler.repository.ScheduledNotificationRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ScheduledNotificationService(
    private val scheduledNotificationRepository: ScheduledNotificationRepository
) {

    @Transactional
    fun lockAndMarkProcessing(): List<ScheduledNotification> {
        val readySchedule = scheduledNotificationRepository.findReadyNotificationsWithLock(LocalDateTime.now(), Pageable.ofSize(100))
        if (readySchedule.isEmpty()) {
            return emptyList()
        }

        scheduledNotificationRepository.updateStatus(readySchedule.map { it.id }, NotificationStatus.IN_PROGRESS)

        return readySchedule
    }

    @Transactional
    fun updateStatus(scheduleId: Long, status: NotificationStatus) {
        scheduledNotificationRepository.updateStatus(scheduleId, status)
    }
}