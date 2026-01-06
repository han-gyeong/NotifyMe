package kr.notifyme.notification.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.entity.NotificationDispatch
import kr.notifyme.notification.repository.NotificationDispatchRepository
import kr.notifyme.notification.repository.NotificationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

class NotificationServiceUnitTest {

    private val notificationRepository: NotificationRepository = mockk()
    private val notificationDispatchRepository: NotificationDispatchRepository = mockk()

    private val notificationService: NotificationService = NotificationService(
        notificationRepository,
        notificationDispatchRepository
    )

    @Test
    fun `알람을 예약할 수 있고 예약시 Dispatch 도 동시에 생성된다`() {
        // given
        val userId = "test"
        val now = LocalDateTime.now()

        val request = NotificationRequest(
            channel = ChannelType.EMAIL,
            message = "message",
            destination = "test@test.com",
            notifyAt = now
        )

        val notificationSlot = slot<Notification>()
        val notificationDispatchSlot = slot<NotificationDispatch>()

        every { notificationRepository.save(capture(notificationSlot)) } answers {
            val entity = notificationSlot.captured
            ReflectionTestUtils.setField(entity, "id", 1L)
            notificationSlot.captured
        }
        every { notificationDispatchRepository.save(capture(notificationDispatchSlot)) } answers {
            notificationDispatchSlot.captured
        }

        // when
        val response = notificationService.scheduleNotification(userId, request)

        // then
        assertAll(
            { assertEquals(1L, response.id) },
            { assertEquals(request.channel, response.channelType) },
            { assertEquals(request.message, response.message) },
            { assertEquals(request.destination, response.destination) },
            { assertEquals(request.notifyAt, response.notifyAt) },
            { assertEquals(userId, response.createdBy) },
            { assertEquals(NotificationStatus.WAITING, response.status) },
        )

        assertEquals(notificationDispatchSlot.captured.notificationId, response.id)
        verify(exactly = 1) { notificationRepository.save(any()) }
        verify(exactly = 1) { notificationDispatchRepository.save(any()) }
    }

    @Test
    fun `대기중인 사용자의 알람을 수정할 수 있다`() {
        // given
        val notificationId = 1L
        val userId = "test"
        val notification = Notification(
            id = notificationId,
            channelType = ChannelType.EMAIL,
            message = "message",
            destination = "test@test.com",
            notifyAt = LocalDateTime.of(2021, 1, 1, 1, 1, 0),
            createdBy = userId,
            status = NotificationStatus.WAITING,
        )

        val request = ModifyNotificationRequest(
            message = "changed-message",
            notifyAt = LocalDateTime.of(2022, 1, 1, 1, 1, 0),
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns notification

        // when
        val response = notificationService.modifyNotification(userId, notificationId, request)

        // then
        assertEquals(request.message, response.message)
        assertEquals(request.notifyAt, response.notifyAt)
    }

    @Test
    fun `해당하는 알람이 없다면 수정시 오류가 발생한다`() {
        // given
        val notificationId = 1L
        val userId = "test"
        val request = ModifyNotificationRequest(
            message = "changed-message",
            notifyAt = LocalDateTime.of(2022, 1, 1, 1, 1, 0),
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns null

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            notificationService.modifyNotification(
                userId = userId,
                notificationId = notificationId,
                request = request)
        }

        assertEquals(exception.message, "No notification with id $notificationId found")
    }

    @Test
    fun `발송 대기 상태가 아니라면 수정시 오류가 발생한다`() {
        // given
        val notificationId = 1L
        val userId = "test"

        val notification = Notification(
            id = notificationId,
            channelType = ChannelType.EMAIL,
            message = "message",
            destination = "test@test.com",
            notifyAt = LocalDateTime.of(2021, 1, 1, 1, 1, 0),
            createdBy = userId,
            status = NotificationStatus.IN_PROGRESS,
        )

        val request = ModifyNotificationRequest(
            message = "changed-message",
            notifyAt = LocalDateTime.of(2022, 1, 1, 1, 1, 0),
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns notification

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            notificationService.modifyNotification(
                userId = userId,
                notificationId = notificationId,
                request = request)
        }

        assertEquals(exception.message, "Cannot modify notification with id $notificationId")
    }
}