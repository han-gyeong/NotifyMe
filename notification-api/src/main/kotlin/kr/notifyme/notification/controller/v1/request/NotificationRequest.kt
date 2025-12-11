package kr.notifyme.notification.controller.v1.request

import kr.notifyme.notification.domain.ChannelType
import java.time.LocalDateTime

data class NotificationRequest(val channel: ChannelType, val message: String, val notifyAt: LocalDateTime)
