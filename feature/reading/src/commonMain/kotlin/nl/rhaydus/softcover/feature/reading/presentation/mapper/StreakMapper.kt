package nl.rhaydus.softcover.feature.reading.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakDayRowUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakDayUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakSheetUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakStripUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.streakSheetScaffoldFor

private val sheetDateFormatter = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
    char(' ')
    day(Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
}

/** [activity] is oldest first; its last entry is today. */
internal fun streakStripFor(
    activity: List<ReadingDayActivity>,
    isLoading: Boolean = false,
): StreakStripUiModel {
    val today = activity.lastOrNull()?.date
    val streakLength = currentStreakLength(activity)

    return StreakStripUiModel(
        days = activity.asReversed().map { day ->
            StreakDayUiModel(
                didRead = day.didRead,
                isToday = day.date == today,
            )
        }.toImmutableList(),
        runningLabel = if (streakLength > 0) {
            "$streakLength ${if (streakLength == 1) "day" else "days"} running"
        } else {
            null
        },
        isLoading = isLoading,
    )
}

/** [activity] is oldest first; its last entry is today. */
internal fun streakSheetFor(activity: List<ReadingDayActivity>): StreakSheetUiModel {
    val today = activity.lastOrNull()?.date

    return StreakSheetUiModel(
        scaffold = streakSheetScaffoldFor(activity = activity),
        rows = activity.asReversed().map { day ->
            StreakDayRowUiModel(
                label = sheetDayLabel(
                    date = day.date,
                    today = today,
                ),
                didRead = day.didRead,
            )
        }.toImmutableList(),
    )
}

/**
 * The number of consecutive read days running backward from today (the last entry in [activity]).
 * Stops at the first missed day; `0` when today itself was missed or the list is empty.
 */
internal fun currentStreakLength(activity: List<ReadingDayActivity>): Int {
    var streak = 0

    for (day in activity.asReversed()) {
        if (day.didRead.not()) break

        streak++
    }

    return streak
}

private fun sheetDayLabel(
    date: LocalDate,
    today: LocalDate?,
): String = when (date) {
    today -> "Today"
    today?.minus(
        1,
        DateTimeUnit.DAY,
    ) -> "Yesterday"
    else -> sheetDateFormatter.format(date)
}
