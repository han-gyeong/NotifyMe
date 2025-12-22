package kr.notifyme.notification.sender.service

import kr.notifyme.notification.sender.dto.SendRequest
import kr.notifyme.notification.sender.dto.SendResult

interface ChannelSender {

    fun canHandle(request: SendRequest): Boolean

    suspend fun send(request: SendRequest): SendResult

}