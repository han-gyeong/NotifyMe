package kr.notifyme.notification.scheduler.repository

import jakarta.persistence.LockModeType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.scheduler.entity.ScheduledNotification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface ScheduledNotificationRepository: JpaRepository<ScheduledNotification, Long> {

    fun findByNotificationId(notificationId: Long): ScheduledNotification?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from ScheduledNotification m where m.status = 'WAITING' and m.notifyAt <= :now")
    fun findReadyNotificationsWithLock(now: LocalDateTime, pageable: Pageable): List<ScheduledNotification>

    @Modifying
    @Query("update ScheduledNotification m set m.status = :status where m.id = :scheduleId")
    fun updateStatus(scheduleId: Long, status: NotificationStatus): Int

    @Modifying
    @Query("update ScheduledNotification m set m.status = :status where m.id in :scheduleIds")
    fun updateStatus(scheduleIds: List<Long>, status: NotificationStatus): Int

}