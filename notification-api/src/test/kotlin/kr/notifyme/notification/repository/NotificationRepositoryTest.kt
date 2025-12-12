package kr.notifyme.notification.repository

import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.fixture.NotificationFixture
import kr.notifyme.notification.fixture.NotificationFixture.Companion.createNotification
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.time.LocalDateTime
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class NotificationRepositoryTest(
    private val notificationRepository: NotificationRepository
) {

    @Test
    fun `본인 등록 알람 리스트 전체 조회하고 페이징이 정상 동작한다`() {
        val pageSize = 10

        repeat(15) { notificationRepository.save(createNotification("user1")) }
        repeat(5) { notificationRepository.save(createNotification("user2")) }

        val firstPage = notificationRepository.findAllByCreatedBy("user1",
            PageRequest.of(0, pageSize))

        val secondPage = notificationRepository.findAllByCreatedBy("user1",
            PageRequest.of(1, pageSize))

        assertThat(firstPage.content).hasSize(pageSize)
        assertThat(firstPage.content).allMatch { it.createdBy == "user1" }
        assertThat(firstPage.hasNext()).isTrue

        assertThat(secondPage.content).hasSize(15 - pageSize)
        assertThat(secondPage.content).allMatch { it.createdBy == "user1" }
        assertThat(secondPage.hasNext()).isFalse
    }

    @Test
    fun `본인이 등록한 알람을 ID로 조회하면 정상 조회된다`() {
        val savedNotification = notificationRepository.save(createNotification("user1"))

        val found = notificationRepository.findByCreatedByAndId(savedNotification.createdBy, savedNotification.id)

        assertThat(found).isNotNull
        assertThat(found?.createdBy).isEqualTo("user1")
    }

    @Test
    fun `본인이 등록하지 않은 알람을 조회하면 조회되지 않는다`() {
        val savedNotification = notificationRepository.save(createNotification("user1"))

        val found = notificationRepository.findByCreatedByAndId("user2", savedNotification.id)

        assertThat(found).isNull()
    }
}