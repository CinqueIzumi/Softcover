package nl.rhaydus.softcover.feature.book_detail.domain.model

import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import java.time.LocalDateTime

data class BookReview(
    val id: Int,
    val reviewDocument: ReviewDocument,
    val hasSpoilers: Boolean,
    val rating: Double?,
    val reviewedAt: String?,
    val likesCount: Int,
    val reviewer: BookReviewer,
) {
    fun getFormattedDate(style: DateStyle): String? {
        val raw = reviewedAt ?: return null

        return runCatching {
            LocalDateTime.parse(raw).format(style.formatter)
        }.getOrNull()
    }
}
