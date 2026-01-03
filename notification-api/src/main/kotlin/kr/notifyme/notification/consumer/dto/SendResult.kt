package kr.notifyme.notification.consumer.dto

import kr.notifyme.notification.domain.ChannelType

data class SendResult(
    val notificationId: Long,
    val channelType: ChannelType,
    val success: Boolean,
    val errorCode: String,
    val errorMessage: String
)
