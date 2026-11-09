package nl.rhaydus.softcover.core.domain.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private const val NANOS_PER_MICRO = 1_000
private const val MICRO_DIGITS = 6

/**
 * Formats this instant like Hardcover's `reading_journals.action_at`: microsecond fraction and an explicit
 * `+00:00` offset, so a locally stamped row sorts as text against synced rows.
 */
fun Instant.toUtcJournalTimestamp(): String {
    val utc = toLocalDateTime(TimeZone.UTC)
    val micros = utc.nanosecond / NANOS_PER_MICRO

    val fraction = micros.toString().padStart(
        MICRO_DIGITS,
        '0',
    )

    return "${utc.date}T${utc.hour.pad()}:${utc.minute.pad()}:${utc.second.pad()}.$fraction+00:00"
}

/**
 * Reads a journal timestamp as the user's local date and time, or null when it does not parse. Accepts an
 * offset form (`action_at`), a naive form, which is read as UTC (`created_at`, rows saved before offsets), and
 * a bare date, which Hardcover also serves for `action_at` and is returned at 00:00.
 *
 * Hardcover stores a date-only event as exactly midnight UTC, so that value keeps its UTC date instead of
 * shifting to the previous day west of UTC.
 */
fun utcJournalTimeToLocalDateTime(
    value: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDateTime? = runCatching {
    val instant = runCatching { Instant.parse(value) }
        .recoverCatching { LocalDateTime.parse(value).toInstant(TimeZone.UTC) }
        .getOrNull()
        ?: return@runCatching LocalDate.parse(value).atTime(
            LocalTime(
                0,
                0,
            ),
        )

    val utc = instant.toLocalDateTime(TimeZone.UTC)

    if (utc.time == LocalTime(
            0,
            0,
        )) utc else instant.toLocalDateTime(timeZone)
}.getOrNull()

fun utcJournalTimeToLocalDate(
    value: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate? = utcJournalTimeToLocalDateTime(
    value,
    timeZone,
)?.date

private fun Int.pad(): String = toString().padStart(
    2,
    '0',
)
