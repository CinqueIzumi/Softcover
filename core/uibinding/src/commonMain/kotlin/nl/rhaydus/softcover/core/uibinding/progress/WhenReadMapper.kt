package nl.rhaydus.softcover.core.uibinding.progress

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.progress.WhenReadUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import kotlin.time.Duration.Companion.minutes

/**
 * Maps the reader's backdate onto the progress sheet's "When did you read this?" row. A `null` receiver
 * is "Just now": the chip is unselected and the editor labels show [now], where a backdate would start.
 */
fun LocalDateTime?.toWhenReadUiModel(now: LocalDateTime): WhenReadUiModel {
    val shown = this ?: now

    val hh = shown.hour.toString().padStart(
        2,
        '0',
    )
    val mm = shown.minute.toString().padStart(
        2,
        '0',
    )

    val dayLabel = when (val daysAgo = shown.date.daysUntil(now.date)) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> "$daysAgo days ago"
    }

    return WhenReadUiModel(
        chip = ChipUiModel(
            key = "when-read",
            label = if (this == null) "Just now" else "$dayLabel, $hh:$mm",
            selected = this != null,
            interaction = ChipInteraction.Clickable,
            leading = ChipLeading.Icon(icon = SoftcoverIcon.DateRange),
            trailing = if (this == null) null else ChipTrailing.Dismiss(label = "Reset to just now"),
        ),
        dayLabel = dayLabel,
        timeLabel = "$hh:$mm",
    )
}

/**
 * The backdate moved by [minutes] (negative is earlier), starting from [now] when there is none, and
 * never later than [now].
 */
fun LocalDateTime?.shiftedBy(
    minutes: Int,
    now: LocalDateTime,
): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()

    val shifted = (this ?: now)
        .toInstant(timeZone)
        .plus(minutes.minutes)
        .toLocalDateTime(timeZone)

    return if (shifted > now) now else shifted
}

/** The ISO-8601 instant the server stamps the mutation with, or `null` for "just now". */
fun LocalDateTime?.toActionAt(): String? = this?.toInstant(TimeZone.currentSystemDefault())?.toString()
