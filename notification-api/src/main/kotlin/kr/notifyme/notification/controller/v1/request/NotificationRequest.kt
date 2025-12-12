package kr.notifyme.notification.controller.v1.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.notifyme.notification.domain.ChannelType
import java.time.LocalDateTime

data class NotificationRequest(

    @field:NotNull
    val channel: ChannelType,

    @field:NotBlank
    val message: String,

    @field:Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val notifyAt: LocalDateTime
)
