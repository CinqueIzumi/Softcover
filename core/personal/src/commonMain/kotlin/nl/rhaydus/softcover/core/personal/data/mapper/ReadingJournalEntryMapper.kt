package nl.rhaydus.softcover.core.personal.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry

private const val METADATA_PAGES_KEY = "progress_pages"
private const val METADATA_SECONDS_KEY = "progress_seconds"

/**
 * Parses one `progress_updated` journal row into a [ReadingJournalEntry]. [metadata] arrives as
 * Apollo's default `jsonb` shape (nested [Map]/[List] of primitives, matching how `review_slate` is
 * parsed elsewhere) and its exact key set is undocumented, so this reads defensively: an entry with
 * neither a page nor a second position is dropped rather than failing the whole list.
 */
internal fun toReadingJournalEntry(
    updatedAt: String,
    metadata: Any?,
): ReadingJournalEntry? {
    val date = parseJournalDateOrNull(value = updatedAt) ?: return null

    val metadataMap = metadata as? Map<*, *> ?: return null

    val pages = metadataMap.intValueOrNull(key = METADATA_PAGES_KEY)
    val seconds = metadataMap.intValueOrNull(key = METADATA_SECONDS_KEY)

    if (pages == null && seconds == null) return null

    return ReadingJournalEntry(
        date = date,
        pages = pages,
        seconds = seconds,
    )
}

private fun Map<*, *>.intValueOrNull(key: String): Int? = when (val value = this[key]) {
    is Number -> value.toInt()
    is String -> value.toIntOrNull()
    else -> null
}

private fun parseJournalDateOrNull(value: String): LocalDate? = runCatching {
    if (value.contains('T')) {
        LocalDateTime.parse(value).date
    } else {
        LocalDate.parse(value)
    }
}.getOrNull()
