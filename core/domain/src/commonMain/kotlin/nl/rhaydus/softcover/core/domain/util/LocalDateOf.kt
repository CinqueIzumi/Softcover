package nl.rhaydus.softcover.core.domain.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun localDateTimeOf(actionAt: String): LocalDateTime =
    Instant.parse(actionAt).toLocalDateTime(TimeZone.currentSystemDefault())

fun localDateOf(actionAt: String): String = localDateTimeOf(actionAt = actionAt).date.toString()
