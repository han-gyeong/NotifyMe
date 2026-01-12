package kr.notifyme.notification.scheduler.service

import kr.notifyme.notification.event.NotificationEvent

interface NotificationEventHandler {

    fun canHandle(event: NotificationEvent): Boolean

    fun handle(event: NotificationEvent)

}