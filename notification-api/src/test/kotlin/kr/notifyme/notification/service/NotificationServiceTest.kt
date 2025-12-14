package kr.notifyme.notification.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.fixture.NotificationFixture
import kr.notifyme.notification.fixture.NotificationFixture.Companion.createNotification
import kr.notifyme.notification.repository.NotificationRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class NotificationServiceTest(
    val notificationService: NotificationService,
    val notificationRepository: NotificationRepository
) {

    @Test
    fun `메시지를 등록한다`() {
        // given
        val userId = "user1"
        val request = NotificationRequest(
            channel = ChannelType.EMAIL,
            message = "HELLO WORLD",
            notifyAt = LocalDateTime.now(),
        )

        // when
        val scheduled = notificationService.scheduleNotification(userId, request)

        // then
        val found = notificationRepository.findById(scheduled.id)
            .orElseThrow()

        assertThat(found.createdBy).isEqualTo(userId)
        assertThat(found.channelType).isEqualTo(request.channel)
        assertThat(found.message).isEqualTo(request.message)
        assertThat(found.notifyAt).isEqualTo(request.notifyAt)

    }

    @Test
    fun `메시지와 내용을 수정할 수 있다`() {
        // given
        val savedNotification = notificationRepository.save(createNotification(userId = "user1"))

        // when
        val modifiedNotifyAt = LocalDateTime.now().plusHours(4)
        val message = "renew message"

        val modifiedNotification = notificationService.modifyNotification(
            "user1",
            savedNotification.id,
            ModifyNotificationRequest(
                message, modifiedNotifyAt
            )
        )

        // then
        val found = notificationRepository.findById(modifiedNotification.id).orElseThrow()

        assertThat(found.message).isEqualTo(message)
        assertThat(found.notifyAt).isEqualTo(modifiedNotifyAt)
    }

    @Test
    fun `등록된 알림이 아니면 수정시 오류가 발생한다`() {
        // given

        // when + then
        val modifiedNotifyAt = LocalDateTime.now().plusHours(4)
        val message = "renew message"

        assertThatThrownBy { notificationService.modifyNotification(
            "user1",
            Long.MAX_VALUE,
            ModifyNotificationRequest(
                message, modifiedNotifyAt
            )
        ) }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No notification")
    }

    @Test
    fun `등록된 알람을 등록한 사용자 ID와 알람 ID로 가져올 수 있다` () {
        // given
        val saved = notificationRepository.save(createNotification(userId = "user1"))

        // when
        val found = notificationService.getNotificationById("user1", saved.id)

        // then
        assertThat(found).isNotNull
        assertThat(found.createdBy).isEqualTo("user1")
    }

    @Test
    fun `등록된 알람과 일치하지 않는 사용자 ID라면 오류가 발생한다`() {
        // given
        val saved = notificationRepository.save(createNotification(userId = "user1"))

        // when + then
        assertThatThrownBy {
            notificationService.getNotificationById("user2", saved.id)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No notification")
    }

    @Test
    fun `등록된 알람과 일치하지 않는 알람ID라면 오류가 발생한다`() {
        // given
        val saved = notificationRepository.save(createNotification(userId = "user1"))

        // when + then
        assertThatThrownBy {
            notificationService.getNotificationById("user1", Long.MAX_VALUE)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No notification")
    }

    @Test
    fun `대기상태의 알람을 취소할 수 있다`() {
        // given
        val saved = notificationRepository.save(createNotification(userId = "user1"))

        // when
        notificationService.cancelNotification(userId = "user1", notificationId = saved.id)

        // then
        val found = notificationRepository.findById(saved.id).orElseThrow()
        assertThat(found.status).isEqualTo(NotificationStatus.CANCELLED)
    }

    @Test
    fun `대기상태가 아닌경우 알람 취소가 실패한다`() {
        // given
        val saved = notificationRepository.save(createNotification(userId = "user1"))
        saved.status = NotificationStatus.IN_PROGRESS

        // when + then
        assertThatThrownBy {
            notificationService.cancelNotification(userId = "user1", notificationId = saved.id)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Cannot cancel")
    }

    @Test
    fun `존재하지 않는 알람을 취소시 오류가 발생한다`() {
        // given

        // when + then
        assertThatThrownBy {
            notificationService.cancelNotification("user1", Long.MAX_VALUE)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No notification")
    }
}