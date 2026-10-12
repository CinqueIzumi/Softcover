package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnUiModel
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric

private const val LOADING_COLUMN_COUNT = 8
private const val LOADING_COLUMN_FRACTION = 0.5f
private const val YEAR_LABEL_MODULUS = 100
private const val YEAR_LABEL_DIGITS = 2
private const val COMPACT_COUNT_THOUSAND = 1_000
private const val COMPACT_COUNT_MILLION = 1_000_000
private const val COMPACT_COUNT_ROUNDS_TO_MILLION = 999_950
private const val EMPTY_CAPTION = "Finish a book and this chart starts filling in."

/** The first busiest year leads; a null [series] yields the loading columns. */
internal fun yearHistoryChartUiModel(
    series: List<YearCount>?,
    metric: YearMetric,
): ColumnChartUiModel {
    if (series == null) {
        return ColumnChartUiModel(
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
    }

    val maxCount = series.maxOfOrNull { it.count } ?: 0
    val peakIndex = if (maxCount > 0) series.indexOfFirst { it.count == maxCount } else -1

    return ColumnChartUiModel(
        columns = series
            .mapIndexed { index, yearCount ->
                ColumnUiModel(
                    fraction = yearCount.count.toFloat() / maxCount.coerceAtLeast(1),
                    tone = when {
                        index == peakIndex -> ChartTone.Lead
                        yearCount.count > 0 -> ChartTone.Second
                        else -> ChartTone.Fifth
                    },
                    label = "'" + (yearCount.year % YEAR_LABEL_MODULUS).toString().padStart(
                        length = YEAR_LABEL_DIGITS,
                        padChar = '0',
                    ),
                    value = if (yearCount.count > 0) formatCompactCount(yearCount.count) else null,
                    contentDescription = yearColumnDescription(
                        yearCount = yearCount,
                        metric = metric,
                    ),
                )
            }
            .toImmutableList(),
        showsValues = true,
    )
}

private fun yearColumnDescription(
    yearCount: YearCount,
    metric: YearMetric,
): String {
    val unit = when (metric) {
        YearMetric.BOOKS -> if (yearCount.count == 1) "book" else "books"
        YearMetric.PAGES -> if (yearCount.count == 1) "page" else "pages"
    }

    return "${yearCount.year}, ${formatGroupedNumber(yearCount.count)} $unit"
}

internal fun yearHistoryCaption(
    series: List<YearCount>?,
    metric: YearMetric,
): String {
    val peak = series?.maxByOrNull { it.count }?.takeIf { it.count > 0 }
        ?: return EMPTY_CAPTION

    val unit = when (metric) {
        YearMetric.BOOKS -> "books"
        YearMetric.PAGES -> "pages"
    }

    return "${peak.year} was your busiest year yet: ${formatGroupedNumber(peak.count)} $unit."
}

/** Full Pages counts (7,753) collide across eight narrow columns, so thousands collapse to "7.8k". */
private fun formatCompactCount(value: Int): String = when {
    value >= COMPACT_COUNT_ROUNDS_TO_MILLION -> {
        val millions = formatDecimalNumber(
            value = value / COMPACT_COUNT_MILLION.toDouble(),
            fractionDigits = 1,
        )

        "${millions}M"
    }

    value >= COMPACT_COUNT_THOUSAND -> {
        val thousands = formatDecimalNumber(
            value = value / COMPACT_COUNT_THOUSAND.toDouble(),
            fractionDigits = 1,
        )

        "${thousands}k"
    }

    else -> value.toString()
}
