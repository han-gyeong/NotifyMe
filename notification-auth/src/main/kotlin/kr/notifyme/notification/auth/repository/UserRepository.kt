package kr.notifyme.notification.auth.repository

import kr.notifyme.notification.auth.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, Long> {

    suspend fun existsByUserId(userId: String): Boolean

    suspend fun existsByEmail(email: String): Boolean

    suspend fun findByUserId(userId: String): User?

}