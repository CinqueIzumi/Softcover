package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import nl.rhaydus.softcover.core.component.chip.ChipFace
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.domain.model.resolve

internal const val ALL_TIME_SCOPE_KEY = "all-time"
internal const val ALL_TIME_SCOPE_LINE = "Showing every year"

private const val YEAR_SCOPE_KEY_PREFIX = "year-"

private val ROLLING_SCOPES = listOf(
    StatsScope.Rolling(StatsWindow.LAST_30_DAYS),
    StatsScope.Rolling(StatsWindow.LAST_6_MONTHS),
    StatsScope.Rolling(StatsWindow.LAST_12_MONTHS),
)

/** The chip key a scope is stored under on the state. */
internal fun statsScopeKey(scope: StatsScope): String = when (scope) {
    is StatsScope.Rolling -> when (scope.window) {
        StatsWindow.LAST_30_DAYS -> "last-30-days"
        StatsWindow.LAST_6_MONTHS -> "last-6-months"
        StatsWindow.LAST_12_MONTHS -> "last-12-months"
    }

    is StatsScope.CalendarYear -> "$YEAR_SCOPE_KEY_PREFIX${scope.year}"
    StatsScope.AllTime -> ALL_TIME_SCOPE_KEY
}

/**
 * The scope row: the rolling windows, one chip per year in [yearsRead] (newest first, as given), then
 * all time. The chip keyed [selectedKey] is selected.
 */
internal fun atlasScopeChips(
    yearsRead: List<Int>,
    selectedKey: String,
): ChipSet<StatsScope> {
    val scopes = ROLLING_SCOPES +
        yearsRead.map { StatsScope.CalendarYear(it) } +
        StatsScope.AllTime

    return scopes
        .map { scope ->
            val key = statsScopeKey(scope)

            ChipUiModel(
                key = key,
                label = scope.chipLabel(),
                selected = key == selectedKey,
                face = ChipFace.Eyebrow,
            ) to scope
        }
        .toChipSet()
}

private fun StatsScope.chipLabel(): String = when (this) {
    is StatsScope.Rolling -> when (window) {
        StatsWindow.LAST_30_DAYS -> "Last 30 days"
        StatsWindow.LAST_6_MONTHS -> "Last 6 months"
        StatsWindow.LAST_12_MONTHS -> "Last 12 months"
    }

    is StatsScope.CalendarYear -> year.toString()
    StatsScope.AllTime -> "All time"
}

/** The quiet line under the scope row; a rolling window names its start year only when it is not [today]'s. */
internal fun atlasScopeLine(
    scope: StatsScope,
    today: LocalDate,
): String = when (scope) {
    is StatsScope.CalendarYear -> "Showing ${scope.year}"
    StatsScope.AllTime -> ALL_TIME_SCOPE_LINE
    is StatsScope.Rolling -> {
        val start = (scope.resolve(today) as StatsRange.Bounded).start
        val month = MonthNames.ENGLISH_FULL.names[start.month.ordinal]
        val year = if (start.year == today.year) "" else " ${start.year}"

        "Showing ${start.day} $month$year to today"
    }
}

/** The trailing words that tie a sentence to this scope ("in 2026"), or null for every year. */
internal fun StatsScope.scopePhrase(): String? = when (this) {
    is StatsScope.Rolling -> when (window) {
        StatsWindow.LAST_30_DAYS -> "in the last 30 days"
        StatsWindow.LAST_6_MONTHS -> "in the last 6 months"
        StatsWindow.LAST_12_MONTHS -> "in the last 12 months"
    }

    is StatsScope.CalendarYear -> "in $year"
    StatsScope.AllTime -> null
}

/** The sentence a section shows in place of an empty chart when [booksRead] is 0 inside a narrower scope. */
internal fun StatsScope.noFinishedBooksSentence(booksRead: Int): String? =
    scopePhrase()?.takeIf { booksRead == 0 }?.let { "No finished books $it." }
