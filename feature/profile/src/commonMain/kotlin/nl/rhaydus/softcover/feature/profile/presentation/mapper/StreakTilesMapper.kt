package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak

private const val CURRENT_EYEBROW = "Current streak"
private const val LONGEST_EYEBROW = "Longest streak"

private val dayMonthFormat = LocalDate.Format {
    day(padding = Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
}

private val dayMonthYearFormat = LocalDate.Format {
    day(padding = Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

/** A null [streak] with [isLoading] yields the masked tile; a null [streak] otherwise reads as no streak running. */
internal fun currentStreakTile(
    streak: ReadingStreak?,
    today: LocalDate,
    isLoading: Boolean = false,
): StatTileUiModel {
    val days = streak?.days ?: 0
    val unit = if (days == 1) "day in a row" else "days in a row"
    val yesterday = today.minus(
        value = 1,
        unit = DateTimeUnit.DAY,
    )

    val caption = when {
        streak == null -> "No streak running. Read today to start one."
        streak.end == yesterday -> "$unit. Read today to keep it."
        else -> unit
    }

    return StatTileUiModel(
        eyebrow = CURRENT_EYEBROW,
        stat = StatNumberUiModel(
            value = days.toDouble(),
            isLoading = isLoading,
        ),
        variant = StatTileVariant.Standard(caption = caption),
    )
}

/** Dates outside [year] carry their year. */
internal fun longestStreakTile(
    streak: ReadingStreak?,
    year: Int,
    isLoading: Boolean = false,
): StatTileUiModel {
    val days = streak?.days ?: 0

    val caption = when {
        streak == null -> "No reading days in $year."
        days == 1 -> "day, ${streak.start.inYear(year)}"
        else -> "days, ${streak.start.inYear(year)} to ${streak.end.inYear(year)}"
    }

    return StatTileUiModel(
        eyebrow = LONGEST_EYEBROW,
        stat = StatNumberUiModel(
            value = days.toDouble(),
            isLoading = isLoading,
        ),
        variant = StatTileVariant.Standard(caption = caption),
    )
}

private fun LocalDate.inYear(year: Int): String =
    if (this.year == year) dayMonthFormat.format(this) else dayMonthYearFormat.format(this)
