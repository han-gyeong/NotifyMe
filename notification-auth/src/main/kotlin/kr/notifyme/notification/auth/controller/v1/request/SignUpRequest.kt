package kr.notifyme.notification.auth.controller.v1.request

data class SignUpRequest(val userId: String, val password: String, val email: String)