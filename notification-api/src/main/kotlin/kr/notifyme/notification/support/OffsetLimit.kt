package kr.notifyme.notification.support

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

data class OffsetLimit(
    val offset: Int,
    var limit: Int
) {
    fun toPageable(): Pageable = PageRequest.of(offset / limit, limit)
}
