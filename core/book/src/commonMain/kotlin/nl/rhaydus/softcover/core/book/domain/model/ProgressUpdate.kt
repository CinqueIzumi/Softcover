package nl.rhaydus.softcover.core.book.domain.model

import kotlin.time.Instant

/** One recorded progress update on a book; [pagesRead] is what that update added, never negative. */
data class ProgressUpdate(
    val bookId: Int,
    val at: Instant,
    val pages: Int?,
    val pagesRead: Int,
)
