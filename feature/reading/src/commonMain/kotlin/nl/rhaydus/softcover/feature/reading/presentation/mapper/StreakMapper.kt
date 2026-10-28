package nl.rhaydus.softcover.feature.reading.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
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

/** The last 21 days ending [today], newest first; empty until [activity] and [today] are known. */
internal fun streakStripFor(
    activity: ReadingActivity?,
    today: LocalDate?,
    isLoading: Boolean = false,
): StreakStripUiModel {
    val streakLength = if (activity == null || today == null) 0 else activity.currentStreak(today)?.days ?: 0

    return StreakStripUiModel(
        days = recentDays(
            activity = activity,
            today = today,
        ).map { day ->
            StreakDayUiModel(
                didRead = day.didRead,
                isToday = day.date == today,
            )
        }.toImmutableList(),
        running = if (streakLength > 0) {
            StatNumberUiModel(
                value = streakLength.toDouble(),
                format = StatNumberFormat.Plain,
                isLoading = isLoading,
                variant = StatNumberVariant.BodySmall,
                suffix = StatNumberSuffix.Plural(
                    one = " day running",
                    other = " days running",
                ),
            )
        } else {
            null
        },
        isLoading = isLoading,
    )
}

/** The last 21 days ending [today], newest first; empty until [activity] and [today] are known. */
internal fun streakSheetFor(
    activity: ReadingActivity?,
    today: LocalDate?,
): StreakSheetUiModel {
    val days = recentDays(
        activity = activity,
        today = today,
    )

    return StreakSheetUiModel(
        scaffold = streakSheetScaffoldFor(
            readDays = days.count { it.didRead },
            dayCount = days.size,
        ),
        rows = days.map { day ->
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

private const val STREAK_WINDOW_DAYS = 21

private data class RecentDay(
    val date: LocalDate,
    val didRead: Boolean,
)

private fun recentDays(
    activity: ReadingActivity?,
    today: LocalDate?,
): List<RecentDay> {
    if (activity == null || today == null) return emptyList()

    return List(STREAK_WINDOW_DAYS) { offset ->
        val date = today.minus(
            value = offset,
            unit = DateTimeUnit.DAY,
        )

        RecentDay(
            date = date,
            didRead = date in activity.pagesByDay,
        )
    }
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
