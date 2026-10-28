package nl.rhaydus.softcover.core.database.model

/** One progress update without its sync bookkeeping, for aggregating reading across every book. */
data class ProgressUpdateRow(
    val bookId: Int,
    val actionAtEpochMillis: Long,
    val pages: Int?,
    val pagesRead: Int,
)
