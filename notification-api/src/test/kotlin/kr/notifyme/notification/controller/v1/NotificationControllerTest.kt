package kr.notifyme.notification.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import kr.notifyme.notification.config.TestWebConfig
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.fixture.NotificationFixture
import kr.notifyme.notification.service.NotificationService
import kr.notifyme.notification.support.OffsetLimit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
@WebMvcTest(NotificationController::class)
@Import(TestWebConfig::class)
class NotificationControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    companion object {
        private const val USER_ID = "TEST-USER-1"
    }

    @MockkBean
    private lateinit var notificationService: NotificationService

    /** 알람 전체 조회 **/
    @Test
    fun `등록된 나의 모든 알림을 가져올 수 있다`() {
        // given
        val content = List(9) {
            NotificationFixture.createNotification(createdBy = USER_ID)
        }

        val slot = slot<OffsetLimit>()
        val slice = SliceImpl(content, PageRequest.of(0, 10), false)

        every { notificationService.getAllNotificationByUserId(USER_ID, capture(slot))} returns slice

        // when + then
        mockMvc.get("/api/v1/notifications") {
            param("offset", "0")
            param("limit", "10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.result") { value("SUCCESS") }
            jsonPath("$.data.hasNext") { value(slice.hasNext()) }
            jsonPath("$.data.content.length()") { value(slice.content.size) }
        }

        assertEquals(slice.pageable.pageSize, slot.captured.limit)
        assertEquals(slice.pageable.offset.toInt(), slot.captured.offset)
    }

    @Test
    fun `페이징 시 Offset이 0보다 작으면 Bad Request`() {
        // given
        val offset = -1
        val limit = 10

        mockMvc.get("/api/v1/notifications") {
            param("offset", "$offset")
            param("limit", "$limit")
        }.andExpect {
            status { isBadRequest() }
        }

        verify { notificationService wasNot Called }
    }

    @Test
    fun `페이징 시 Limit 0이면 Bad Request`() {
        // given
        val offset = 10
        val limit = 0

        // when + then
        mockMvc.get("/api/v1/notifications") {
            param("offset", "$offset")
            param("limit", "$limit")
        }.andExpect {
            status { isBadRequest() }
        }

        verify { notificationService wasNot Called }
    }

    /** 알람 개별 조회 **/
    @Test
    fun `알람 ID로 내가 등록한 알람을 가져올 수 있다`() {
        // given
        val created = NotificationFixture.createNotification(createdBy = USER_ID)

        every { notificationService.getNotificationById(USER_ID, created.id) } returns created

        // when + then
        mockMvc.get("/api/v1/notifications/{notificationId}", created.id) {

        }.andExpect {
            status { isOk() }
            jsonPath("$.data.channel") { value(created.channelType.toString()) }
            jsonPath("$.data.destination") { value(created.destination) }
            jsonPath("$.data.message") { value(created.message) }
            jsonPath("$.data.notifyAt") { value(created.notifyAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
        }
    }

    /** 알람 등록 **/
    @Test
    fun `알람을 등록할 수 있다`() {
        // given
        val request = NotificationRequest(ChannelType.EMAIL, "HELLO", "hello@example.com",LocalDateTime.now().plusHours(1))
        val notification = NotificationFixture.createNotification(
            channelType = ChannelType.EMAIL,
            message = request.message,
            destination = request.destination,
            notifyAt = request.notifyAt)

        every { notificationService.scheduleNotification(USER_ID, any(NotificationRequest::class)) } returns notification

        // when + then
        mockMvc.post("/api/v1/notifications") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(notification.id) }
            jsonPath("$.data.channel") { value(notification.channelType.toString()) }
            jsonPath("$.data.destination") { value(notification.destination) }
            jsonPath("$.data.message") { value(notification.message) }
            jsonPath("$.data.notifyAt") { value(notification.notifyAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
        }
    }

    @Test
    fun `과거 시간 알람을 등록하면 Bad Request`() {
        // given
        val notifyAt = LocalDateTime.now().minusHours(1)
        val request = NotificationRequest(ChannelType.EMAIL, "HELLO", "hello@example.com", notifyAt)

        // when + then
        mockMvc.post("/api/v1/notifications") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify { notificationService wasNot Called }
    }

    @Test
    fun `메시지가 비어있다면 Bad Request`() {
        // given
        val message = ""
        val request = NotificationRequest(ChannelType.EMAIL, message, "hello@example.com", LocalDateTime.now().minusHours(1))

        mockMvc.post("/api/v1/notifications") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify { notificationService wasNot Called }
    }

    @Test
    fun `정의되지 않은 채널 타입이라면 Bad Request`() {
        // given
        val request = NotificationRequest(ChannelType.EMAIL, "HELLO", "hello@example.com", LocalDateTime.now().minusHours(1))
        val map = objectMapper.convertValue(request, Map::class.java).toMutableMap()
        map["channel"] = "HELLO"

        // when + then
        mockMvc.post("/api/v1/notifications") {
            content = objectMapper.writeValueAsString(map)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify { notificationService wasNot Called }
    }

    @Test
    fun `알람을 수정할 수 있다`() {
        // given
        val notificationId = 1L
        val request = ModifyNotificationRequest(
            message = "RENEW",
            notifyAt = LocalDateTime.now().plusHours(2)
        )

        val notification = NotificationFixture.createNotification(
            id = notificationId,
            message = request.message,
            notifyAt = request.notifyAt,
            status = NotificationStatus.WAITING
        )

        every { notificationService.modifyNotification(USER_ID, any(), any()) } returns notification

        // when & then
        mockMvc.patch("/api/v1/notifications/{notificationId}", notification.id) {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(notification.id) }
            jsonPath("$.data.message") { value(request.message) }
            jsonPath("$.data.notifyAt") { value(request.notifyAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
        }
    }

    @Test
    fun `알람을 취소할 수 있다`() {
        // given
        val notificationId = 1L
        val canceled = NotificationFixture.createNotification(
            id = notificationId,
            status = NotificationStatus.CANCELLED
        )

        every { notificationService.cancelNotification(USER_ID, notificationId) } returns canceled

        // when & then
        mockMvc.patch("/api/v1/notifications/{notificationId}/cancel", canceled.id) {

        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(canceled.id) }
            jsonPath("$.data.status") { value(NotificationStatus.CANCELLED.toString()) }
        }
    }
}