package nl.rhaydus.softcover.core.domain.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private const val NANOS_PER_MICRO = 1_000
private const val MICRO_DIGITS = 6

/**
 * Formats this instant like Hardcover's `reading_journals.updated_at`: naive UTC with no zone marker and a
 * fixed-width microsecond fraction, so a locally stamped row sorts correctly against synced rows as text.
 */
fun Instant.toUtcJournalTimestamp(): String {
    val utc = toLocalDateTime(TimeZone.UTC)
    val micros = utc.nanosecond / NANOS_PER_MICRO
    val fraction = micros.toString().padStart(
        MICRO_DIGITS,
        '0',
    )

    return "${utc.date}T${utc.hour.pad()}:${utc.minute.pad()}:${utc.second.pad()}.$fraction"
}

/** Reads a naive-UTC journal timestamp as the user's local date and time, or null when it does not parse. */
fun utcJournalTimeToLocalDateTime(value: String): LocalDateTime? = runCatching {
    LocalDateTime.parse(value)
        .toInstant(TimeZone.UTC)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}.getOrNull()

/** The user's local calendar day for a naive-UTC journal timestamp, or null when it does not parse. */
fun utcJournalTimeToLocalDate(value: String): LocalDate? = utcJournalTimeToLocalDateTime(value)?.date

private fun Int.pad(): String = toString().padStart(
    2,
    '0',
)
