package nl.rhaydus.softcover.core.book.domain.model

import nl.rhaydus.softcover.core.domain.model.ReviewDocument

/** What Hardcover currently holds for one user book, as far as a queued write needs to know it is already applied. */
data class ServerUserBookState(
    val statusCode: Int?,
    val rating: Double?,
    val reviewDocument: ReviewDocument?,
    val reviewHasSpoilers: Boolean,
    val reads: List<ServerUserBookRead>,
)
