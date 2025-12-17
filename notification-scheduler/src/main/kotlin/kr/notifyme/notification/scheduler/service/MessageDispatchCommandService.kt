package kr.notifyme.notification.scheduler.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.scheduler.entity.NotificationDispatch
import kr.notifyme.notification.scheduler.repository.NotificationDispatchRepository
import kr.notifyme.notification.scheduler.repository.NotificationRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MessageDispatchCommandService(
    private val notificationRepository: NotificationRepository,
    private val notificationDispatchRepository: NotificationDispatchRepository
) {

    @Value("\${notification.scheduler.id}")
    private lateinit var schedulerId: String

    @Value("\${notification.scheduler.lock-until-minutes}")
    private lateinit var lockUntilMinutes: String

    @Transactional
    fun claimDispatch(lockToken: String): List<NotificationDispatch> {
        val claimCnt = notificationDispatchRepository.claimDispatch(
            lockedBy = schedulerId,
            lockToken = lockToken,
            lockedUntil = LocalDateTime.now().plusMinutes(lockUntilMinutes.toLong()),
            current = LocalDateTime.now())

        if (claimCnt == 0) {
            return listOf()
        }

        return notificationDispatchRepository.findAllByLockedByAndLockToken(
            lockedBy = schedulerId,
            lockToken = lockToken)
    }

    @Transactional
    fun markEnqueued(notificationId: Long): Int {
        return notificationRepository.updateStatus(notificationId, NotificationStatus.IN_PROGRESS)
    }

    @Transactional
    fun markFailed(notificationId: Long): Int {
        return notificationRepository.updateStatus(notificationId, NotificationStatus.FAILED)
    }
}