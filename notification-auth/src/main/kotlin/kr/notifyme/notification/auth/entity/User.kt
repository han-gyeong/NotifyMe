package kr.notifyme.notification.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(

    @Id
    val id: Long? = null,

    val userId: String,

    val email: String,

    val password: String,
)
