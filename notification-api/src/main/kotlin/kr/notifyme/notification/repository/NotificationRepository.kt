package kr.notifyme.notification.repository

import kr.notifyme.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, String> {

    fun findAllByCreatedBy(createdBy: String) : List<Notification>

    fun findByCreatedByAndId(createdBy: String, notificationId: String) : Notification?

}