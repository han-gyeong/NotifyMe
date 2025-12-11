package kr.notifyme.notification.support.response

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean
)
