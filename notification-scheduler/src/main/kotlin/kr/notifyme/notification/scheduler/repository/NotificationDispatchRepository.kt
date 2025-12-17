package kr.notifyme.notification.scheduler.repository

import kr.notifyme.notification.scheduler.entity.NotificationDispatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface NotificationDispatchRepository : JpaRepository<NotificationDispatch, Long> {

    @Modifying
    @Query("""
        update notification_dispatch d
        set d.locked_by    = :lockedBy,
            d.lock_token   = :lockToken,
            d.locked_until = :lockedUntil
        where exists(
                select 'x'
                from notification n
                where n.id = d.notification_id
                  and n.status = 'WAITING'
                  and n.notify_at <= :current
            )
          and d.locked_until is null
        limit 100
    """, nativeQuery = true)
    fun claimDispatch(lockedBy: String, lockToken: String, lockedUntil: LocalDateTime, current: LocalDateTime): Int

    fun findAllByLockedByAndLockToken(lockedBy: String, lockToken: String): List<NotificationDispatch>

}