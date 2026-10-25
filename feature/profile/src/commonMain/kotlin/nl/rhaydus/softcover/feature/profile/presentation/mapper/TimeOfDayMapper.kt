package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.HourlyPages
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import kotlin.math.roundToInt

internal const val TIME_OF_DAY_TITLE = "When you read"
internal const val TIME_OF_DAY_FOOTNOTE = "Each column covers three hours from the time shown."
private const val LOADING_CAPTION = "Your pages by time of day. You read most after 21:00."
private const val EMPTY_ALL_TIME = "Update your progress and your hours start to show."
private const val HOURS_IN_DAY = 24
private const val HOURS_PER_BLOCK = 3
private const val FIRST_BLOCK_HOUR = 6
private const val BLOCK_COUNT = HOURS_IN_DAY / HOURS_PER_BLOCK
private const val LOADING_COLUMN_FRACTION = 0.5f
private const val PERCENT = 100
private const val BELOW_ONE_PERCENT = "<1%"
private const val ABOVE_NINETY_NINE_PERCENT = ">99%"

/** The phrase that finishes "You read most", per block in chart order from 06. */
private val MOST_PHRASES = listOf(
    "early, before 09:00",
    "in the morning",
    "around midday",
    "in the afternoon",
    "in the early evening",
    "after 21:00",
    "after midnight",
    "in the small hours",
)

/** Eight masked columns with their real labels, since the labels never change. */
internal fun loadingTimeOfDayChartUiModel(): ColumnChartUiModel = ColumnChartUiModel(
    columns = List(BLOCK_COUNT) { block ->
        ColumnUiModel(
            fraction = LOADING_COLUMN_FRACTION,
            tone = ChartTone.Second,
            label = hourLabel(block.startHour()),
            value = "",
            contentDescription = "",
        )
    }.toImmutableList(),
    showsValues = true,
    isLoading = true,
)

internal fun loadingTimeOfDayCaption(): String = LOADING_CAPTION

/** A null [hourly] yields the loading columns. */
internal fun timeOfDayChartUiModel(hourly: HourlyPages?): ColumnChartUiModel {
    if (hourly == null) return loadingTimeOfDayChartUiModel()

    val totals = hourly.blockTotals()
    val sum = totals.sum()
    val peak = totals.max()
    val tones = rankedColumnTones(totals)

    return ColumnChartUiModel(
        columns = totals
            .mapIndexed { block, total ->
                val share = shareLabel(
                    total = total,
                    sum = sum,
                )

                ColumnUiModel(
                    fraction = total.toFloat() / peak.coerceAtLeast(1),
                    tone = tones[block],
                    label = hourLabel(block.startHour()),
                    value = share,
                    contentDescription = readout(
                        block = block,
                        total = total,
                        share = share,
                    ),
                )
            }
            .toImmutableList(),
        showsValues = true,
    )
}

/** The caption under the chart; a null [hourly] yields the loading one. */
internal fun timeOfDayCaption(
    hourly: HourlyPages?,
    scope: StatsScope,
): String {
    if (hourly == null) return loadingTimeOfDayCaption()

    val peakBlock = peakIndex(hourly.blockTotals())
        ?: return scope.scopePhrase()?.let { "No pages read $it." } ?: EMPTY_ALL_TIME

    return "Your pages by time of day. You read most ${MOST_PHRASES[peakBlock]}."
}

internal fun hasTimeOfDayPages(hourly: HourlyPages?): Boolean = hourly?.pages?.any { it > 0 } == true

/** Folds the 24 hours into eight blocks of three, starting at 06, so the last one ends at 06. */
private fun HourlyPages.blockTotals(): List<Int> = List(BLOCK_COUNT) { block ->
    (0 until HOURS_PER_BLOCK).sumOf { offset -> pages[(block.startHour() + offset) % HOURS_IN_DAY] }
}

private fun Int.startHour(): Int = (FIRST_BLOCK_HOUR + this * HOURS_PER_BLOCK) % HOURS_IN_DAY

private fun hourLabel(hour: Int): String = hour.toString().padStart(
    2,
    '0',
)

private fun shareLabel(
    total: Int,
    sum: Int,
): String {
    if (total == 0) return percentLabel(fraction = 0f)

    val share = total.toFloat() / sum
    val rounded = (share * PERCENT).roundToInt()

    return when {
        rounded == 0 -> BELOW_ONE_PERCENT
        rounded == PERCENT && total < sum -> ABOVE_NINETY_NINE_PERCENT
        else -> percentLabel(fraction = share)
    }
}

private fun readout(
    block: Int,
    total: Int,
    share: String,
): String {
    val start = block.startHour()
    val range = "${hourLabel(start)}:00 to ${hourLabel((start + HOURS_PER_BLOCK) % HOURS_IN_DAY)}:00"

    val spoken = when {
        total == 0 -> return "$range, no pages"
        share == BELOW_ONE_PERCENT -> "less than 1%"
        share == ABOVE_NINETY_NINE_PERCENT -> "more than 99%"
        else -> share
    }

    return "$range, $spoken of your pages"
}
