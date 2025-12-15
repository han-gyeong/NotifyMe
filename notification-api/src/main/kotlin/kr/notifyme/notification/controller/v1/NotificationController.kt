package kr.notifyme.notification.controller.v1

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import kr.notifyme.notification.controller.v1.request.ModifyNotificationRequest
import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.controller.v1.response.NotificationResponse
import kr.notifyme.notification.service.NotificationService
import kr.notifyme.notification.support.OffsetLimit
import kr.notifyme.notification.support.auth.CurrentUserId
import kr.notifyme.notification.support.response.ApiResponse
import kr.notifyme.notification.support.response.PageResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping()
    fun getAllMyNotifications(
        @CurrentUserId userId: String,
        @RequestParam @PositiveOrZero offset: Int,
        @RequestParam @Positive limit: Int) : ApiResponse<PageResponse<NotificationResponse>> {
        val notifications = notificationService.getAllNotificationByUserId(userId, OffsetLimit(offset, limit))

        return ApiResponse.success(
            PageResponse(
                notifications.content.map(NotificationResponse::of),
                notifications.hasNext()
            )
        )
    }

    @GetMapping("/{notificationId}")
    fun getNotificationById(
        @CurrentUserId userId: String,
        @PathVariable notificationId: Long
    ) : ApiResponse<NotificationResponse> {
        var notification = notificationService.getNotificationById(userId, notificationId)
            .let { NotificationResponse.of(it) }

        return ApiResponse.success(notification)
    }

    @PostMapping
    fun registerNotification(
        @CurrentUserId userId: String,
        @Valid @RequestBody notificationRequest: NotificationRequest
    ) : ApiResponse<NotificationResponse> {
        val notification = notificationService.scheduleNotification(userId, notificationRequest)
            .let { NotificationResponse.of(it) }

        return ApiResponse.success(notification)
    }

    @PatchMapping("/{notificationId}")
    fun modifyNotification(
        @CurrentUserId userId: String,
        @PathVariable notificationId: Long,
        @Valid @RequestBody modifyNotificationRequest: ModifyNotificationRequest,
    ) : ApiResponse<NotificationResponse> {
        val notification = notificationService.modifyNotification(userId, notificationId, modifyNotificationRequest)
            .let { NotificationResponse.of(it) }

        return ApiResponse.success(notification)
    }

    @PatchMapping("/{notificationId}/cancel")
    fun cancelNotification(
        @CurrentUserId userId: String,
        @PathVariable notificationId: Long
    ) : ApiResponse<NotificationResponse> {
        val notification = notificationService.cancelNotification(userId, notificationId)
            .let { NotificationResponse.of(it) }

        return ApiResponse.success(notification);
    }
}