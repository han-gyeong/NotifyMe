package kr.notifyme.notification.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import kr.notifyme.notification.config.TestWebConfig
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.domain.ChannelType
import kr.notifyme.notification.domain.NotificationStatus
import kr.notifyme.notification.entity.Notification
import kr.notifyme.notification.fixture.NotificationFixture
import kr.notifyme.notification.service.NotificationService
import kr.notifyme.notification.support.OffsetLimit
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
@WebMvcTest(NotificationController::class)
@Import(TestWebConfig::class)
class NotificationControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var notificationService: NotificationService

    /** 알람 전체 조회 **/
    @Test
    fun `등록된 나의 모든 알림을 가져올 수 있다`() {
        // given
        val content = listOf(
            NotificationFixture.createNotification("TEST-USER-1"),
            NotificationFixture.createNotification("TEST-USER-1"),
            NotificationFixture.createNotification("TEST-USER-1")
        )

        val captor = argumentCaptor<OffsetLimit>()
        val slice = SliceImpl(content, PageRequest.of(0, 10), false)

        whenever(notificationService.getAllNotificationByUserId(eq("TEST-USER-1"), captor.capture()))
            .thenReturn(slice)

        // when + then
        mockMvc.perform(
            get("/api/v1/notifications")
                .param("offset", "0")
                .param("limit", "10")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.hasNext").value(slice.hasNext()))
            .andExpect(jsonPath("$.data.content.length()").value(content.size))
        .andDo(MockMvcResultHandlers.print())

        assertThat(captor.firstValue.limit).isEqualTo(slice.pageable.pageSize)
        assertThat(captor.firstValue.offset).isEqualTo(slice.pageable.offset)
    }

    /** 알람 개별 조회 **/
    @Test
    fun `알람 ID로 내가 등록한 알람을 가져올 수 있다`() {
        // given
        val created = NotificationFixture.createNotification("TEST-USER-1")

        whenever(notificationService.getNotificationById(eq("TEST-USER-1"), any()))
            .thenReturn(created)

        // when + then
        mockMvc.perform(
            get("/api/v1/notifications/{notificationId}", 1)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.channel").value(created.channelType.toString()))
            .andExpect(jsonPath("$.data.message").value(created.message))
            .andExpect(jsonPath("$.data.notifyAt").value(created.notifyAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
    }

    /** 알람 등록 **/
    @Test
    fun `알람을 등록할 수 있다`() {
        // given
        val request = NotificationRequest(ChannelType.EMAIL, "HELLO", LocalDateTime.now().plusHours(1))
        val response = Notification(
            id = 0L,
            channelType = request.channel,
            message = request.message,
            notifyAt = request.notifyAt,
            status = NotificationStatus.WAITING,
            createdBy = "TEST-USER-1"
        )

        whenever(notificationService.scheduleNotification(eq("TEST-USER-1"), any()))
            .thenReturn(response)

        // when + then
        mockMvc.perform(
            post("/api/v1/notifications")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(response.id))
            .andExpect(jsonPath("$.data.channel").value(request.channel.toString()))
            .andExpect(jsonPath("$.data.message").value(request.message))
            .andExpect(jsonPath("$.data.notifyAt").value(request.notifyAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `과거 시간 알람을 등록하면 Bad Request`() {
        // given
        val request = NotificationRequest(ChannelType.EMAIL, "HELLO", LocalDateTime.now().minusHours(1))

        // when + then
        mockMvc.perform(
            post("/api/v1/notifications")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)

        verifyNoInteractions(notificationService)
    }

    @Test
    fun `메시지가 비어있다면 Bad Request`() {
        // given
        val request = NotificationRequest(ChannelType.EMAIL, "", LocalDateTime.now().minusHours(1))

        // when + then
        mockMvc.perform(
            post("/api/v1/notifications")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)

        verifyNoInteractions(notificationService)
    }

    @Test
    fun `정의되지 않은 채널 타입이라면 Bad Request`() {
        // given
        val request = """
            {
                "channel" : "HELLO",
                "message" : "HELLO",
                "notifyAt" : "2055-10-12 09:15:30",
        """.trimIndent()

        // when + then
        mockMvc.perform(
            post("/api/v1/notifications")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)

        verifyNoInteractions(notificationService)
    }

    @Test
    fun `알람을 수정할 수 있다`() {
        // given
        val notificationId = 1L
        val request = ModifyNotificationRequest(
            message = "RENEW",
            notifyAt = LocalDateTime.now().plusHours(2)
        )

        val modified = Notification(
            id = notificationId,
            channelType = ChannelType.EMAIL,
            message = request.message,
            notifyAt = request.notifyAt,
            status = NotificationStatus.WAITING,
            createdBy = "TEST-USER-1"
        )

        whenever(notificationService.modifyNotification(eq("TEST-USER-1"), eq(notificationId), any()))
            .thenReturn(modified)

        // when + then
        mockMvc.perform(
            patch("/api/v1/notifications/{notificationId}", notificationId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(notificationId))
            .andExpect(jsonPath("$.data.message").value(request.message))
            .andExpect(jsonPath("$.data.notifyAt").value(request.notifyAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
    }

    @Test
    fun `알람을 취소할 수 있다`() {
        // given
        val notificationId = 1L
        val cancelled = Notification(
            id = notificationId,
            channelType = ChannelType.EMAIL,
            message = "HELLO",
            notifyAt = LocalDateTime.now().plusHours(1),
            status = NotificationStatus.CANCELLED,
            createdBy = "TEST-USER-1"
        )

        whenever(notificationService.cancelNotification(eq("TEST-USER-1"), eq(notificationId)))
            .thenReturn(cancelled)

        // when + then
        mockMvc.perform(
            patch("/api/v1/notifications/{notificationId}/cancel", notificationId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").value(notificationId))
            .andExpect(jsonPath("$.data.message").value(cancelled.message))
            .andExpect(jsonPath("$.data.status").value(NotificationStatus.CANCELLED.toString()))
    }
}