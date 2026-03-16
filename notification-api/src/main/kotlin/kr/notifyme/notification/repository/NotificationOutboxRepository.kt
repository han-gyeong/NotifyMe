package kr.notifyme.notification.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import kr.notifyme.notification.entity.NotificationOutbox
import kr.notifyme.notification.entity.OutboxStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.*
import java.time.LocalDateTime

interface NotificationOutboxRepository: JpaRepository<NotificationOutbox, Long> {

    fun findByEventId(eventId: String): NotificationOutbox?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // SKIP LOCK
    @Query("select m from NotificationOutbox m " +
            "where m.status = :status " +
            "and (m.nextRetryAt IS NULL or m.nextRetryAt <= :now) " +
            "and m.retryCount < :maxRetryCount " +
            "order by m.nextRetryAt "
    )
    fun findEventForRetry(status: OutboxStatus, now: LocalDateTime, maxRetryCount: Int, pageable: Pageable): List<NotificationOutbox>

    @Modifying
    @Query("update NotificationOutbox m " +
            "set m.retryCount = :retryCount, m.nextRetryAt = :nextRetryAt " +
            "where m.eventId = :eventId")
    fun updateNextRetry(eventId: String, retryCount: Int, nextRetryAt: LocalDateTime)

    @Modifying
    @Query("update NotificationOutbox m set m.status = :outboxStatus where m.eventId = :eventId")
    fun updateOutboxStatus(eventId: String, outboxStatus: OutboxStatus)



}