package kr.notifyme.notification.repository

import kr.notifyme.notification.entity.NotificationOutbox
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationOutboxRepository: JpaRepository<NotificationOutbox, Long> {

    fun findByEventId(eventId: String): NotificationOutbox?

}