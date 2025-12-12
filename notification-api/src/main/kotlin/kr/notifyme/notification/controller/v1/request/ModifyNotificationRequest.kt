package kr.notifyme.notification.controller.v1.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime

data class ModifyNotificationRequest(

    @field:NotEmpty
    val message: String,

    @field:Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val notifyAt: LocalDateTime

)
