package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import nl.rhaydus.softcover.core.component.chart.YearGridCell
import nl.rhaydus.softcover.core.component.chart.YearGridMonthUiModel
import nl.rhaydus.softcover.core.component.chart.YearGridUiModel
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity

private const val MONTH_COUNT = 12
private const val FEWER_LABEL = "Fewer pages"
private const val MORE_LABEL = "More"
private val MONTH_LABELS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** The masked grid shown until the first year of days lands. */
internal fun loadingYearGrid(): YearGridUiModel = yearGrid(
    activity = ReadingActivity(
        pagesByDay = emptyMap(),
        streaks = emptyList(),
    ),
    year = LOADING_YEAR,
    isLoading = true,
)

/**
 * One cell per calendar day of the year. Read days are tinted by quartile of that year's page counts with
 * value cut points, so equal counts share a step; with fewer than four reading days, or every count equal,
 * every read day is the darkest step.
 */
internal fun yearGrid(
    activity: ReadingActivity,
    year: Int,
    isLoading: Boolean = false,
): YearGridUiModel {
    val pagesByDay = activity.pagesByDay.filterKeys { it.year == year }
    val cutPoints = quartileCutPoints(pagesByDay.values)

    return YearGridUiModel(
        year = year,
        months = List(MONTH_COUNT) { monthIndex ->
            val first = LocalDate(
                year = year,
                month = monthIndex + 1,
                day = 1,
            )

            val length = first
                .plus(
                    value = 1,
                    unit = DateTimeUnit.MONTH,
                )
                .minus(
                    value = 1,
                    unit = DateTimeUnit.DAY,
                )
                .day

            YearGridMonthUiModel(
                label = MONTH_LABELS[monthIndex],
                days = List(length) { dayIndex ->
                    val date = LocalDate(
                        year = year,
                        month = monthIndex + 1,
                        day = dayIndex + 1,
                    )
                    val pages = pagesByDay[date]

                    if (pages == null) {
                        YearGridCell.Empty
                    } else {
                        YearGridCell.Read(
                            date = date,
                            step = stepFor(
                                pages = pages,
                                cutPoints = cutPoints,
                            ),
                            contentDescription = readDayDescription(
                                date = date,
                                pages = pages,
                            ),
                        )
                    }
                }.toImmutableList(),
            )
        }.toImmutableList(),
        fewerLabel = FEWER_LABEL,
        moreLabel = MORE_LABEL,
        isLoading = isLoading,
    )
}

private fun readDayDescription(
    date: LocalDate,
    pages: Int,
): String {
    val unit = if (pages == 1) "page" else "pages"

    return "${date.day} ${monthName(date.month)}, $pages $unit"
}

private fun monthName(month: Month): String = month.name.lowercase().replaceFirstChar { it.uppercase() }
