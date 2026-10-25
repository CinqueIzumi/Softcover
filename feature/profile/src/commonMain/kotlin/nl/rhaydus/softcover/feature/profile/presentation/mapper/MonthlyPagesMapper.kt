package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.yearMonth
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.resolve
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter

private val MONTHLY_PAGES_EYEBROW = atlasChapterEyebrow(AtlasChapter.SEASONS_AND_RECORDS)
private const val EMPTY_HEADLINE = "A year in pages"
private const val LOADING_HEADLINE = "A March to remember"
private const val LOADING_DESCRIPTION = "Your busiest month was March, at 1,480 pages."
private const val EMPTY_ALL_TIME = "Update your progress and the months start filling in."
private val VOWEL_MONTHS = setOf(4, 8, 10)
private const val LOADING_COLUMN_COUNT = 12
private const val LOADING_COLUMN_FRACTION = 0.5f
private const val LABEL_STRIDE = 3
private const val ALL_TIME_MONTH_SET_KEY = "1/12"

/** One chart column, whichever shape the stats carry. [year] is null across every year. */
private data class MonthColumn(
    val year: Int?,
    val month: Int,
    val pages: Int,
    val isStillToCome: Boolean,
    val startDay: Int?,
)

/** The masked header shown until the first stats land. */
internal fun loadingMonthlyPagesHeader(): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = MONTHLY_PAGES_EYEBROW,
    headline = LOADING_HEADLINE,
    description = LOADING_DESCRIPTION,
    isLoading = true,
)

/** Twelve masked columns, the count of a calendar year. */
internal fun loadingMonthlyPagesChartUiModel(): ColumnChartUiModel = ColumnChartUiModel(
    columns = List(LOADING_COLUMN_COUNT) {
        ColumnUiModel(
            fraction = LOADING_COLUMN_FRACTION,
            tone = ChartTone.Second,
            label = "",
            value = "",
            contentDescription = "",
        )
    }.toImmutableList(),
    showsValues = true,
    isLoading = true,
)

/** The chapter header, headlined by the busiest month; a null [monthlyPages] yields the masked one. */
internal fun monthlyPagesHeader(
    monthlyPages: MonthlyPages?,
    scope: StatsScope,
    today: LocalDate,
): SectionHeaderUiModel.Section {
    if (monthlyPages == null) return loadingMonthlyPagesHeader()

    val columns = monthlyPages.monthColumns(
        scope = scope,
        today = today,
    )
    val busiest = columns.busiest()

    return SectionHeaderUiModel.Section(
        eyebrow = MONTHLY_PAGES_EYEBROW,
        headline = busiest?.headline() ?: EMPTY_HEADLINE,
        description = monthlyPagesDescription(
            busiest = busiest,
            scope = scope,
            columns = columns,
        ),
    )
}

/** A null [monthlyPages] yields the loading columns. */
internal fun monthlyPagesChartUiModel(
    monthlyPages: MonthlyPages?,
    scope: StatsScope,
    today: LocalDate,
): ColumnChartUiModel {
    if (monthlyPages == null) return loadingMonthlyPagesChartUiModel()

    val columns = monthlyPages.monthColumns(
        scope = scope,
        today = today,
    )
    val peak = columns.maxOfOrNull { it.pages } ?: 0
    val tones = rankedColumnTones(columns.map { it.pages })

    return ColumnChartUiModel(
        columns = columns
            .mapIndexed { index, column ->
                ColumnUiModel(
                    fraction = column.pages.toFloat() / peak.coerceAtLeast(1),
                    tone = tones[index],
                    label = column.shortName().takeIf { index % LABEL_STRIDE == 0 || columns.size <= LABEL_STRIDE },
                    value = formatGroupedNumber(column.pages).takeIf { tones[index] == ChartTone.Lead },
                    contentDescription = column.readout(),
                )
            }
            .toImmutableList(),
        showsValues = true,
    )
}

internal fun hasMonthlyPages(monthlyPages: MonthlyPages?): Boolean = when (val pages = monthlyPages) {
    is MonthlyPages.Span -> pages.months.any { it.count > 0 }
    is MonthlyPages.AcrossYears -> pages.pages.any { it > 0 }
    null -> false
}

/**
 * Identifies the month labels a chart shows: the first month and the column count. The labels carry no
 * year, so a calendar year and all time share a key and animate bars in place; null while loading.
 */
internal fun monthlyPagesMonthSetKey(monthlyPages: MonthlyPages?): String? = when (val pages = monthlyPages) {
    is MonthlyPages.Span -> pages.months.firstOrNull()?.let { "${it.month}/${pages.months.size}" }
    is MonthlyPages.AcrossYears -> ALL_TIME_MONTH_SET_KEY
    null -> null
}

private fun MonthlyPages.monthColumns(
    scope: StatsScope,
    today: LocalDate,
): List<MonthColumn> = when (val pages = this) {
    is MonthlyPages.Span -> {
        val range = scope.resolve(today) as? StatsRange.Bounded
        val partialStartDay = range?.start?.day?.takeIf { it != 1 }

        pages.months.mapIndexed { index, month ->
            MonthColumn(
                year = month.year,
                month = month.month,
                pages = month.count,
                isStillToCome = month.count == 0 && YearMonth(
                    year = month.year,
                    month = month.month,
                ) > today.yearMonth,
                startDay = partialStartDay.takeIf { index == 0 },
            )
        }
    }

    is MonthlyPages.AcrossYears -> pages.pages.mapIndexed { index, count ->
        MonthColumn(
            year = null,
            month = index + 1,
            pages = count,
            isStillToCome = false,
            startDay = null,
        )
    }
}

private fun MonthColumn.shortName(): String = MonthNames.ENGLISH_ABBREVIATED.names[month - 1]

private fun MonthColumn.fullName(): String = MonthNames.ENGLISH_FULL.names[month - 1]

private fun MonthColumn.readout(): String {
    val name = fullName()

    return when {
        year == null -> "$name, ${pagesPhrase(pages)} across every year"
        isStillToCome -> "$name $year, still to come"
        startDay != null -> "From $startDay $name $year, ${pagesPhrase(pages)}"
        else -> "$name $year, ${pagesPhrase(pages)}"
    }
}

private fun pagesPhrase(pages: Int): String = if (pages == 1) "1 page" else "${formatGroupedNumber(pages)} pages"

private fun List<MonthColumn>.busiest(): MonthColumn? = peakIndex(map { it.pages })?.let { get(it) }

private fun MonthColumn.headline(): String {
    val article = if (month in VOWEL_MONTHS) "An" else "A"

    return "$article ${fullName()} to remember"
}

private fun monthlyPagesDescription(
    busiest: MonthColumn?,
    scope: StatsScope,
    columns: List<MonthColumn>,
): String {
    if (busiest == null) {
        return scope.scopePhrase()?.let { "No pages read $it." } ?: EMPTY_ALL_TIME
    }

    val pages = pagesPhrase(busiest.pages)

    if (busiest.year == null) {
        return "Across every year, your busiest month is ${busiest.fullName()}, at $pages."
    }

    val spansYears = columns.first().year != columns.last().year
    val month = if (spansYears) "${busiest.fullName()} ${busiest.year}" else busiest.fullName()
    val sentence = "Your busiest month was $month, at $pages."
    val tail = columns.filter { it.isStillToCome }.stillToComeSentence()

    return if (tail == null) sentence else "$sentence $tail"
}

private fun List<MonthColumn>.stillToComeSentence(): String? = when (size) {
    0 -> null
    1 -> "${first().fullName()} is still to come."
    2 -> "${first().fullName()} and ${last().fullName()} are still to come."
    else -> "${first().fullName()} to ${last().fullName()} are still to come."
}
