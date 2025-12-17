package kr.notifyme.notification.scheduler.repository

import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.scheduler.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NotificationRepository : JpaRepository<Notification, Long> {

    @Modifying
    @Query("update Notification n set n.status = :status where n.id = :notificationId")
    fun updateStatus(notificationId: Long, status: NotificationStatus): Int

}