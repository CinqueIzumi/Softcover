package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionSegmentUiModel
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.PERCENTAGE_MULTIPLIER
import kotlin.math.roundToInt

private const val LOADING_SEGMENT_COUNT = 3

/**
 * Known genders take `Lead`, `Second`, `Third` in rank order; [Gender.Unknown] stays out of that indexing
 * and renders as `Unknown`. A null [slices] yields the loading chart.
 */
internal fun genderChartUiModel(slices: List<GenderSlice>?): ProportionChartUiModel {
    if (slices == null) return loadingProportionChart()

    val knownGenders = slices.filter { it.gender != Gender.Unknown }

    return ProportionChartUiModel(
        segments = slices
            .map { slice ->
                ProportionSegmentUiModel(
                    label = slice.gender.toDisplayLabel(),
                    share = "${(slice.fraction * PERCENTAGE_MULTIPLIER).roundToInt()}%",
                    fraction = slice.fraction.toFloat(),
                    tone = when (slice.gender) {
                        Gender.Unknown -> ChartTone.Unknown
                        else -> ChartTone.forRank(knownGenders.indexOf(slice))
                    },
                )
            }
            .toImmutableList(),
    )
}

/**
 * Yes and No are always segments, even at 0%; Unknown only when someone is untagged. A null [breakdown]
 * yields the loading chart.
 */
internal fun demographicChartUiModel(breakdown: DemographicBreakdown?): ProportionChartUiModel {
    if (breakdown == null) return loadingProportionChart()

    val total = breakdown.total.coerceAtLeast(1)

    val segments = listOfNotNull(
        demographicSegment(
            label = "Yes",
            count = breakdown.yesCount,
            total = total,
            tone = ChartTone.Lead,
        ),
        demographicSegment(
            label = "No",
            count = breakdown.noCount,
            total = total,
            tone = ChartTone.Fourth,
        ),
        demographicSegment(
            label = "Unknown",
            count = breakdown.unknownCount,
            total = total,
            tone = ChartTone.Unknown,
        ).takeIf { breakdown.unknownCount > 0 },
    )

    return ProportionChartUiModel(segments = segments.toImmutableList())
}

private fun demographicSegment(
    label: String,
    count: Int,
    total: Int,
    tone: ChartTone,
): ProportionSegmentUiModel {
    val fraction = count.toDouble() / total

    return ProportionSegmentUiModel(
        label = label,
        share = "${(fraction * PERCENTAGE_MULTIPLIER).roundToInt()}%",
        fraction = fraction.toFloat(),
        tone = tone,
    )
}

private fun loadingProportionChart(): ProportionChartUiModel = ProportionChartUiModel(
    segments = List(LOADING_SEGMENT_COUNT) { index ->
        ProportionSegmentUiModel(
            label = "",
            share = "",
            fraction = 0f,
            tone = ChartTone.forRank(index),
        )
    }.toImmutableList(),
    isLoading = true,
)

/**
 * `Other` stays the plain word: the bucket mixes non-binary and trans authors, and a narrower label
 * would misgender real people.
 */
private fun Gender.toDisplayLabel(): String = when (this) {
    Gender.Female -> "Women"
    Gender.Male -> "Men"
    Gender.Other -> "Other"
    Gender.Unknown -> "Unknown"
}
