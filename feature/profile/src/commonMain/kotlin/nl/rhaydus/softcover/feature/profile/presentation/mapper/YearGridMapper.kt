package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import nl.rhaydus.softcover.core.component.chart.YearGridCell
import nl.rhaydus.softcover.core.component.chart.YearGridMonthUiModel
import nl.rhaydus.softcover.core.component.chart.YearGridStep
import nl.rhaydus.softcover.core.component.chart.YearGridUiModel
import nl.rhaydus.softcover.core.profile.domain.model.DailyPages
import kotlin.math.ceil

private const val MONTH_COUNT = 12
private const val MIN_DAYS_FOR_QUARTILES = 4
private const val FIRST_QUARTILE = 0.25
private const val MEDIAN = 0.5
private const val THIRD_QUARTILE = 0.75
private const val FEWER_LABEL = "Fewer pages"
private const val MORE_LABEL = "More"
private val MONTH_LABELS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** The masked grid shown until the first year of days lands. */
internal fun loadingYearGrid(): YearGridUiModel = yearGrid(
    daily = DailyPages(
        year = LOADING_YEAR,
        pages = emptyMap(),
    ),
    isLoading = true,
)

/**
 * One cell per calendar day of the year. Read days are tinted by quartile of that year's page counts with
 * value cut points, so equal counts share a step; with fewer than four reading days, or every count equal,
 * every read day is the darkest step.
 */
internal fun yearGrid(
    daily: DailyPages,
    isLoading: Boolean = false,
): YearGridUiModel {
    val cutPoints = quartileCutPoints(daily.pages.values)

    return YearGridUiModel(
        year = daily.year,
        months = List(MONTH_COUNT) { monthIndex ->
            val first = LocalDate(
                year = daily.year,
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
                        year = daily.year,
                        month = monthIndex + 1,
                        day = dayIndex + 1,
                    )
                    val pages = daily.pages[date]

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

private data class QuartileCutPoints(
    val first: Int,
    val second: Int,
    val third: Int,
)

private fun quartileCutPoints(values: Collection<Int>): QuartileCutPoints? {
    if (values.size < MIN_DAYS_FOR_QUARTILES || values.toSet().size == 1) return null

    val sorted = values.sorted()

    return QuartileCutPoints(
        first = nearestRank(
            sorted = sorted,
            fraction = FIRST_QUARTILE,
        ),
        second = nearestRank(
            sorted = sorted,
            fraction = MEDIAN,
        ),
        third = nearestRank(
            sorted = sorted,
            fraction = THIRD_QUARTILE,
        ),
    )
}

private fun nearestRank(
    sorted: List<Int>,
    fraction: Double,
): Int {
    val rank = ceil(fraction * sorted.size).toInt()
    val index = (rank - 1).coerceIn(
        minimumValue = 0,
        maximumValue = sorted.lastIndex,
    )

    return sorted[index]
}

private fun stepFor(
    pages: Int,
    cutPoints: QuartileCutPoints?,
): YearGridStep = when {
    cutPoints == null -> YearGridStep.Four
    pages <= cutPoints.first -> YearGridStep.One
    pages <= cutPoints.second -> YearGridStep.Two
    pages <= cutPoints.third -> YearGridStep.Three
    else -> YearGridStep.Four
}

private fun readDayDescription(
    date: LocalDate,
    pages: Int,
): String {
    val unit = if (pages == 1) "page" else "pages"

    return "${date.day} ${monthName(date.month)}, $pages $unit"
}

private fun monthName(month: Month): String = month.name.lowercase().replaceFirstChar { it.uppercase() }
