package kr.notifyme.notification.sender.dto

import kr.notifyme.notification.domain.ChannelType

data class SendRequest(
    val notificationId: Long,
    val channelType: ChannelType,
    val destination: String,
    val message: String,
)
