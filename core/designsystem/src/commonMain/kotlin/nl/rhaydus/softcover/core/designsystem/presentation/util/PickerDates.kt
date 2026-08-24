package nl.rhaydus.softcover.core.designsystem.presentation.util

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Material 3's `DatePickerState` defines both `initialSelectedDateMillis` and `selectedDateMillis` as
 * **UTC** milliseconds from the epoch: it canonicalises whatever it is handed to start-of-day UTC, and
 * hands back start-of-day UTC for the day the user tapped. Converting either side through the device's
 * timezone shifts the calendar day — west of UTC the confirmed date lands a day early, east of it the
 * dialog opens on the day (and at a month boundary, the month) before the one it was given.
 *
 * These two helpers are the single place that speaks the picker's UTC contract. They are deliberately
 * `internal`: the only way out of this module is
 * [nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverDatePickerDialog], which takes
 * and returns a [LocalDate], so no feature ever handles the millis itself.
 */
internal fun LocalDate.toPickerMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

internal fun Long.toPickerLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.UTC)
    .date
