package nl.rhaydus.softcover.core.designsystem.presentation.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/** Today's date in the system time zone. */
fun currentLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/** The current date-and-time in the system time zone. */
fun currentLocalDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/** The current instant — callers compute elapsed durations from it (e.g. a live reading-session timer). */
fun currentInstant(): Instant = Clock.System.now()
