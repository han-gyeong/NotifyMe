package kr.notifyme.notification.service

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.fixture.NotificationFixture
import kr.notifyme.notification.repository.NotificationDispatchRepository
import kr.notifyme.notification.repository.NotificationRepository
import java.time.LocalDateTime

class NotificationServiceUnitKoTest: BehaviorSpec({
    val notificationRepository: NotificationRepository = mockk()
    val notificationDispatchRepository: NotificationDispatchRepository = mockk()
    val notificationService = NotificationService(notificationRepository, notificationDispatchRepository)

    Given("알림 예약 요청이 들어왔을때") {
        val userId = "test"
        val now = LocalDateTime.now()

        val request = NotificationRequest(
            channel = ChannelType.EMAIL,
            message = "message",
            destination = "test@test.com",
            notifyAt = now
        )

        every { notificationRepository.save(any()) } answers { firstArg() }
        every { notificationDispatchRepository.save(any()) } answers { firstArg() }

        When("알람을 등록하면") {
            val response = notificationService.scheduleNotification(userId, request)

            Then("예약된 알람 정보가 반환되어야 한다") {
                assertSoftly {
                    response.channelType shouldBe request.channel
                    response.message shouldBe request.message
                    response.destination shouldBe request.destination
                    response.notifyAt shouldBe request.notifyAt
                    response.createdBy shouldBe userId
                    response.status shouldBe NotificationStatus.WAITING
                }

                verify(exactly = 1) { notificationRepository.save(any()) }
            }

            Then("알람 발송 정보도 동시에 저장된다") {
                verify {
                    notificationDispatchRepository.save(
                        match { it.notificationId == response.id }
                    )
                }
            }
        }
    }

    Given("수정 가능한 대기상태의 알람이 있는 경우") {
        val notificationId = 1L
        val userId = "test"
        val notification = NotificationFixture.createNotification(
            id = notificationId,
            createdBy = userId
        )

        val request = ModifyNotificationRequest(
            message = "changed-message",
            notifyAt = LocalDateTime.of(2022, 1, 1, 1, 1, 0),
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns notification

        When("알람을 수정하면") {
            val response = notificationService.modifyNotification(userId, notificationId, request)

            Then("알람의 정보가 변경되어야 한다") {
                assertSoftly {
                    response.message shouldBe request.message
                    response.notifyAt shouldBe request.notifyAt
                }
            }
        }
    }

    Given("수정하려는 알람이 존재하지 않는 경우") {
        val notificationId = 1L
        val userId = "test"
        val request = ModifyNotificationRequest(
            message = "changed-message",
            notifyAt = LocalDateTime.of(2022, 1, 1, 1, 1, 0),
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns null

        When("알람 수정을 요청하면") {
            Then("예외가 발생하며 오류 메시지가 반환된다") {
                shouldThrow<IllegalArgumentException> {
                    notificationService.modifyNotification(userId, notificationId, request)
                }.message shouldContain "No notification with"
            }
        }
    }

    Given("수정 가능한 상태의 알람이 아닌 경우") {
        val notificationId = 1L
        val userId = "test"

        val notification = NotificationFixture.createNotification(
            id = notificationId,
            createdBy = userId,
            status = NotificationStatus.IN_PROGRESS,
        )

        val request = ModifyNotificationRequest(
            message = "changed-message",
            notifyAt = LocalDateTime.of(2022, 1, 1, 1, 1, 0),
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns notification

        When("알람 수정을 요청하면") {
            Then("예외가 발생하며 오류 메시지가 반환된다") {
                shouldThrow<IllegalArgumentException> {
                    notificationService.modifyNotification(userId, notificationId, request)
                }.message shouldContain "Cannot modify"
            }
        }
    }

    Given("특정 ID 로 사용자가 등록한 알람이 존재할때") {
        val notificationId = 1L
        val userId = "test"
        val notification = NotificationFixture.createNotification(
            id = notificationId,
            createdBy = userId,
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns notification

        When("ID 로 조회를 요청하면") {
            val response = notificationService.getNotificationById(userId, notificationId)

            Then("알람 정보가 반환되어야 한다") {
                response shouldBe notification
            }
        }
    }

    Given("특정 ID 로 사용자가 등록한 알람이 존재하지 않을 때") {
        val notificationId = 1L
        val userId = "test"

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns null

        When("ID로 조회를 요청하면") {
            Then("예외가 발생하고 오류 메시지가 반환된다") {
                shouldThrow<IllegalArgumentException> {
                    notificationService.getNotificationById(userId, notificationId)
                }.message shouldContain "No notification with"
            }
        }
    }

    Given("사용자가 등록한 취소 가능 상태 알람이 존재할 경우") {
        val userId = "test"
        val notificationId = 1L
        val notification = NotificationFixture.createNotification(
            id = notificationId,
            createdBy = userId,
            status = NotificationStatus.WAITING,
        )

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns notification

        When("알람 취소를 요청하면") {
            val response = notificationService.cancelNotification(userId, notificationId)

            Then("알람이 정상적으로 취소된다") {
                response.status shouldBe NotificationStatus.CANCELLED
            }
        }
    }

    Given("취소하려는 알람이 존재하지 않는 경우") {
        val userId = "test"
        val notificationId = 1L

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns null

        When("알람 취소를 요청하면") {
            Then("예외가 발생하고 오류 메시지가 반환된다") {
                shouldThrow<IllegalArgumentException> {
                    notificationService.cancelNotification(userId, notificationId)
                }.message shouldContain "No notification with"
            }
        }
    }

    Given("알람이 취소 가능하지 않은 상태일 경우") {
        val userId = "test"
        val notificationId = 1L
        val notification: Notification = mockk()

        every { notificationRepository.findByCreatedByAndId(userId, notificationId) } returns notification
        every { notification.canCancel() } returns false

        When("알람 취소를 요청하면") {
            Then("예외가 발생하고 오류 메시지가 반환된다") {
                shouldThrow<IllegalArgumentException> {
                    notificationService.cancelNotification(userId, notificationId)
                }.message shouldContain "Cannot cancel notification"
            }
        }
    }
})