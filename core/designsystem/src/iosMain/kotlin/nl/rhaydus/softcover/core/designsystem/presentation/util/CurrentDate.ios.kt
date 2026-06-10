package nl.rhaydus.softcover.core.designsystem.presentation.util

import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

actual fun currentLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

actual fun currentLocalDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

actual fun currentInstant(): Instant = Clock.System.now()
