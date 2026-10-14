package nl.rhaydus.softcover.core.personal.data.mapper

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import kotlin.time.Instant

private const val METADATA_PAGES_KEY = "progress_pages"
private const val METADATA_SECONDS_KEY = "progress_seconds"

/**
 * Parses one `progress_updated` journal row into a synced [ProgressHistoryEntity]. [metadata] arrives as
 * Apollo's default `jsonb` shape (nested [Map]/[List] of primitives, matching how `review_slate` is
 * parsed elsewhere) and its exact key set is undocumented, so this reads defensively: an entry with
 * neither a page nor a second position is dropped rather than failing the whole list.
 */
internal fun toProgressHistoryEntity(
    bookId: Int,
    actionAt: String,
    metadata: Any?,
): ProgressHistoryEntity? {
    val instant = runCatching { Instant.parse(actionAt) }.getOrNull() ?: return null

    val metadataMap = metadata as? Map<*, *> ?: return null

    val pages = metadataMap.intValueOrNull(key = METADATA_PAGES_KEY)
    val seconds = metadataMap.intValueOrNull(key = METADATA_SECONDS_KEY)

    if (pages == null && seconds == null) return null

    return ProgressHistoryEntity(
        bookId = bookId,
        actionAtEpochMillis = instant.toEpochMilliseconds(),
        pages = pages,
        seconds = seconds,
        pending = false,
    )
}

internal fun ProgressHistoryEntity.toReadingJournalEntry(timeZone: TimeZone): ReadingJournalEntry =
    ReadingJournalEntry(
        date = Instant.fromEpochMilliseconds(actionAtEpochMillis).toLocalDateTime(timeZone).date,
        pages = pages,
        seconds = seconds,
    )

private fun Map<*, *>.intValueOrNull(key: String): Int? = when (val value = this[key]) {
    is Number -> value.toInt()
    is String -> value.toIntOrNull()
    else -> null
}
