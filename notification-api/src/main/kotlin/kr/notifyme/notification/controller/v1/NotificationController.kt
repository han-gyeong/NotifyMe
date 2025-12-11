package kr.notifyme.notification.controller.v1

import kr.notifyme.notification.controller.v1.request.NotificationRequest
import kr.notifyme.notification.controller.v1.response.NotificationResponse
import kr.notifyme.notification.service.NotificationService
import kr.notifyme.notification.support.OffsetLimit
import kr.notifyme.notification.support.response.ApiResponse
import kr.notifyme.notification.support.response.PageResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping()
    fun getAllMyNotifications(
        @RequestHeader("X-USER-ID") userId: String,
        @RequestParam offset: Int,
        @RequestParam limit: Int) : ApiResponse<PageResponse<NotificationResponse>> {
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
        @RequestHeader("X-USER-ID") userId: String,
        @PathVariable notificationId: String
    ) : ApiResponse<NotificationResponse> {
        var notification = notificationService.getNotificationById(userId, notificationId)
            .let { NotificationResponse.of(it) }

        return ApiResponse.success(notification)
    }

    @PostMapping
    fun registerNotification(
        @RequestHeader("X-USER-ID") userId: String,
        @RequestBody notificationRequest: NotificationRequest
    ) : ApiResponse<NotificationResponse> {
        var notification = notificationService.scheduleNotification(userId, notificationRequest)
            .let { NotificationResponse.of(it) }

        return ApiResponse.success(notification)
    }

    @PatchMapping("/{notificationId}/cancel")
    fun cancelNotification(
        @RequestHeader("X-USER-ID") userId: String,
        @PathVariable notificationId: String
    ) : ApiResponse<NotificationResponse> {
        val notification = notificationService.cancelNotification(userId, notificationId)
            .let { NotificationResponse.of(it) }

        return ApiResponse.success(notification);
    }
}