package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartSliceUiModel
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.profile.presentation.state.DemographicBreakdownUiModel

private const val LOADING_SEGMENT_COUNT = 3

/** The gender chart with its caption; a null [demographics] yields the loading chart and no data. */
internal fun genderBreakdownUiModel(
    demographics: AuthorDemographics?,
    hideUntaggedAuthors: Boolean,
): DemographicBreakdownUiModel = DemographicBreakdownUiModel(
    chart = genderChartUiModel(demographics?.genderSlices),
    caption = genderCaption(
        knownGenderCount = demographics?.knownGenderCount ?: 0,
        unknownGenderCount = demographics?.unknownGenderCount ?: 0,
        hideUntaggedAuthors = hideUntaggedAuthors,
    ),
    hasData = demographics?.genderSlices.orEmpty().isNotEmpty(),
)

/** A Yes/No/Unknown chart with its caption; a null [breakdown] yields the loading chart and no data. */
internal fun demographicBreakdownUiModel(
    breakdown: DemographicBreakdown?,
    hideUntaggedAuthors: Boolean,
): DemographicBreakdownUiModel = DemographicBreakdownUiModel(
    chart = demographicChartUiModel(breakdown),
    caption = demographicCaption(
        breakdown = breakdown ?: DemographicBreakdown(),
        hideUntaggedAuthors = hideUntaggedAuthors,
    ),
    hasData = (breakdown?.total ?: 0) > 0,
)

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
                ChartSliceUiModel(
                    label = slice.gender.toDisplayLabel(),
                    share = percentLabel(fraction = slice.fraction.toFloat()),
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
): ChartSliceUiModel {
    val fraction = count.toDouble() / total

    return ChartSliceUiModel(
        label = label,
        share = percentLabel(fraction = fraction.toFloat()),
        fraction = fraction.toFloat(),
        tone = tone,
    )
}

private fun loadingProportionChart(): ProportionChartUiModel = ProportionChartUiModel(
    segments = List(LOADING_SEGMENT_COUNT) { index ->
        ChartSliceUiModel(
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

// While the untagged authors are hidden the count is the same arithmetic (unknownGenderCount is 0 by
// then) but a different claim, so the wording says which authors it counted.
private fun genderCaption(
    knownGenderCount: Int,
    unknownGenderCount: Int,
    hideUntaggedAuthors: Boolean,
): String {
    val totalAuthorCount = knownGenderCount + unknownGenderCount

    if (totalAuthorCount == 0) return "This fills in as your authors get gender data on Hardcover."

    if (hideUntaggedAuthors) return "Across $totalAuthorCount authors with a known gender."

    return "Across $totalAuthorCount authors."
}

private fun demographicCaption(
    breakdown: DemographicBreakdown,
    hideUntaggedAuthors: Boolean,
): String {
    if (breakdown.total == 0) {
        if (hideUntaggedAuthors) return "None of your authors carry this data yet."

        return "This fills in as your authors get tagged on Hardcover."
    }

    val counts = listOfNotNull(
        "Yes ${breakdown.yesCount}",
        "No ${breakdown.noCount}",
        "Unknown ${breakdown.unknownCount}".takeIf { breakdown.unknownCount > 0 },
    ).joinToString(separator = " · ")
    val total = if (hideUntaggedAuthors) {
        "of ${breakdown.total} tagged authors"
    } else {
        "of ${breakdown.total} authors"
    }

    return "$counts · $total"
}
