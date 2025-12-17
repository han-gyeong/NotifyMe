package kr.notifyme.notification.scheduler.dto

import kr.notifyme.notification.domain.ChannelType

data class SendRequest(
    val notificationId: Long,
    val channelType: ChannelType,
    val destination: String,
    val message: String,
)
