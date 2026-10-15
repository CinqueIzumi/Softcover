package nl.rhaydus.softcover.core.book.domain.model

/** The server's newest unfinished read for a user book, as far as a queued progress write needs it. */
data class UnfinishedRead(
    val id: Int,
    val startedAt: String?,
)
