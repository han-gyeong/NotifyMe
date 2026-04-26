package kr.notifyme.notification.scheduler.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.entity.OutboxStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.*
import java.time.LocalDateTime

interface NotificationOutboxRepository : JpaRepository<NotificationOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query(
        """
        select o from NotificationOutbox o
        where o.status = :status
        and o.nextPublishAt <= :now
        and o.retryCount < :maxRetryCount
        order by o.nextPublishAt, o.notificationId
        """
    )
    fun findReadyOutboxes(
        status: OutboxStatus,
        now: LocalDateTime,
        maxRetryCount: Int,
        pageable: Pageable
    ): List<NotificationOutbox>

    @Modifying
    @Query("update NotificationOutbox o set o.status = :status where o.notificationId = :notificationId")
    fun updateStatus(notificationId: Long, status: OutboxStatus): Int

    @Modifying
    @Query(
        """
        update NotificationOutbox o
        set o.retryCount = o.retryCount + 1, o.nextPublishAt = :nextRetryAt
        where o.notificationId = :notificationId
        """
    )
    fun updateNextRetry(notificationId: Long, nextRetryAt: LocalDateTime): Int
}
