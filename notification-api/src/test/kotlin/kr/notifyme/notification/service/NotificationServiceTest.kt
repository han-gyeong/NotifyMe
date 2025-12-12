package kr.notifyme.notification.service

import jakarta.transaction.Transactional
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
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
}