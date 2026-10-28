package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import nl.rhaydus.softcover.core.component.chart.RecentDaysGridUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingSummaryStatUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingSummaryUiModel

private const val RECENT_WINDOW_CAPTION = "The last 12 weeks."

/** Loading stats and a masked grid until [history], [activity] and [today] have all arrived. */
internal fun readingSummaryUiModel(
    history: ReadingHistory?,
    activity: ReadingActivity?,
    today: LocalDate?,
): ReadingSummaryUiModel {
    val isLoading = history == null || activity == null || today == null
    val year = today?.year ?: 0
    val pagesPerRecentDay = if (activity == null || today == null) {
        emptyList()
    } else {
        List(RecentDaysGridUiModel.DAY_COUNT) { index ->
            activity.pagesByDay[
                today.minus(
                    value = RecentDaysGridUiModel.DAY_COUNT - 1 - index,
                    unit = DateTimeUnit.DAY,
                ),
            ]
        }
    }
    val recentDaysCaption = if (isLoading) {
        RECENT_WINDOW_CAPTION
    } else {
        val readDays = pagesPerRecentDay.count { it != null }

        "$RECENT_WINDOW_CAPTION You read on $readDays of ${RecentDaysGridUiModel.DAY_COUNT} days."
    }

    return ReadingSummaryUiModel(
        header = SectionHeaderUiModel.Inline(eyebrow = "Your reading"),
        stats = persistentListOf(
            summaryStat(
                count = history?.booksByYear.countFor(year),
                isLoading = isLoading,
                caption = "books this year",
            ),
            summaryStat(
                count = history?.pagesByYear.countFor(year),
                isLoading = isLoading,
                caption = "pages",
            ),
            summaryStat(
                count = activity?.readingDaysIn(year) ?: 0,
                isLoading = isLoading,
                caption = "reading days",
            ),
        ),
        recentDays = recentDaysGrid(
            pagesPerDay = pagesPerRecentDay,
            contentDescription = recentDaysCaption,
            isLoading = isLoading,
        ),
        recentDaysCaption = recentDaysCaption,
        linkLabel = "Open your Reading Atlas",
    )
}

/** [pagesPerDay] runs oldest first; tints are quartiles of the window's own page counts. */
private fun recentDaysGrid(
    pagesPerDay: List<Int?>,
    contentDescription: String,
    isLoading: Boolean,
): RecentDaysGridUiModel {
    val cutPoints = quartileCutPoints(pagesPerDay.filterNotNull())

    return RecentDaysGridUiModel(
        days = List(RecentDaysGridUiModel.DAY_COUNT) { index ->
            pagesPerDay.getOrNull(index)?.let { pages ->
                stepFor(
                    pages = pages,
                    cutPoints = cutPoints,
                )
            }
        }.toImmutableList(),
        contentDescription = contentDescription,
        isLoading = isLoading,
    )
}

private fun summaryStat(
    count: Int,
    isLoading: Boolean,
    caption: String,
): ReadingSummaryStatUiModel = ReadingSummaryStatUiModel(
    stat = StatNumberUiModel(
        value = count.toDouble(),
        format = StatNumberFormat.Grouped,
        isLoading = isLoading,
        variant = StatNumberVariant.Headline,
    ),
    caption = caption,
)

private fun List<YearCount>?.countFor(year: Int): Int = this?.firstOrNull { it.year == year }?.count ?: 0
