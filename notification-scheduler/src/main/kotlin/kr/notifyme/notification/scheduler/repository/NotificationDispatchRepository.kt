package kr.notifyme.notification.scheduler.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.*
import java.time.LocalDateTime

interface NotificationDispatchRepository: JpaRepository<Notification, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // SKIP LOCK
    @Query("select m from Notification m where m.status = :status and m.notifyAt <= :now order by m.notifyAt, m.id")
    fun findReadyNotificationsWithLock(
        status: NotificationStatus,
        now: LocalDateTime,
        pageable: Pageable
    ): List<Notification>

    @Modifying
    @Query("update Notification m set m.status = :toStatus where m.id in :scheduleIds")
    fun updateStatus(scheduleIds: List<Long>, toStatus: NotificationStatus): Int

    @Modifying
    @Query("update Notification m set m.status = :toStatus where m.id = :notificationId")
    fun updateStatus(notificationId: Long, toStatus: NotificationStatus): Int

}
