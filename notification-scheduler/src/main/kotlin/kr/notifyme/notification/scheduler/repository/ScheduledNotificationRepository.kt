package kr.notifyme.notification.scheduler.repository

import kr.notifyme.notification.scheduler.entity.ScheduledNotification
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduledNotificationRepository: JpaRepository<ScheduledNotification, Long> {

    fun findByNotificationId(notificationId: Long): ScheduledNotification?

}