package kr.notifyme.notification.repository

import kr.notifyme.notification.entity.NotificationDispatch
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationDispatchRepository : JpaRepository<NotificationDispatch, Long> {
}