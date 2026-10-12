package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution

private const val RATINGS_HISTOGRAM_EYEBROW = "How you score"
private const val LOADING_STEP_COUNT = 9
private const val LOADING_STEP_FRACTION = 0.5f

/** Half-star buckets, with only the whole stars labelled. A null [ratings] yields the loading columns. */
internal fun ratingsChartUiModel(ratings: RatingsDistribution?): ColumnChartUiModel {
    if (ratings == null) {
        return ColumnChartUiModel(
            columns = List(LOADING_STEP_COUNT) { index ->
                ColumnUiModel(
                    fraction = LOADING_STEP_FRACTION,
                    tone = if (index % 2 == 0) ChartTone.Lead else ChartTone.Fourth,
                    label = if (index % 2 == 0) "" else null,
                    value = null,
                    contentDescription = "",
                )
            }.toImmutableList(),
            showsValues = false,
            isLoading = true,
        )
    }

    val buckets = ratings.halfStarBuckets
    val maxCount = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)

    return ColumnChartUiModel(
        columns = buckets
            .mapIndexed { index, count ->
                val isWholeStep = index % 2 == 0

                ColumnUiModel(
                    fraction = count.toFloat() / maxCount,
                    tone = when {
                        count == 0 -> ChartTone.Fifth
                        isWholeStep -> ChartTone.Lead
                        else -> ChartTone.Fourth
                    },
                    label = if (isWholeStep) "${index / 2 + 1}★" else null,
                    value = null,
                    contentDescription = ratingColumnDescription(
                        index = index,
                        count = count,
                    ),
                )
            }
            .toImmutableList(),
        showsValues = false,
    )
}

private fun ratingColumnDescription(
    index: Int,
    count: Int,
): String {
    val half = if (index % 2 == 1) "½" else ""
    val stars = "${index / 2 + 1}$half"
    val starUnit = if (index == 0) "star" else "stars"
    val bookUnit = if (count == 1) "book" else "books"

    return "$stars $starUnit, $count $bookUnit"
}

internal fun ratingsAverageCaption(totalRatings: Int): String {
    val bookUnit = if (totalRatings == 1) "BOOK" else "BOOKS"

    return "AVERAGE ACROSS $totalRatings $bookUnit"
}

internal fun ratingsAverageUiModel(ratings: RatingsDistribution?): StatNumberUiModel = StatNumberUiModel(
    value = ratings?.average ?: 0.0,
    format = StatNumberFormat.Decimal,
    isLoading = ratings == null,
    variant = StatNumberVariant.Average,
)

/**
 * The "How you score" header, with a headline chosen from [RatingBand] (average + distribution shape),
 * never from the bare average.
 */
internal fun RatingBand.toRatingsHistogramHeader(): SectionHeaderUiModel.Section =
    SectionHeaderUiModel.Section(
        eyebrow = RATINGS_HISTOGRAM_EYEBROW,
        headline = toRatingsHistogramHeadline(),
    )

internal fun RatingBand.toRatingsHistogramCaption(): String = when (this) {
    RatingBand.GENEROUS ->
        "Half stars count. Most of yours land between 4 and 5."

    RatingBand.SUPERFAN ->
        "Your average is above 4.5. When you finish a book, you tend to love it."

    RatingBand.CENTRIST ->
        "Most of your ratings sit around 3.5. You rarely go very high or very low."

    RatingBand.EXACTING ->
        "Your average runs low, so five stars from you means something."

    RatingBand.POLARISED ->
        "Your ratings land high or low, with little in between."

    RatingBand.NEW_READER ->
        "Rate a few more books and a pattern will show up here."
}

private fun RatingBand.toRatingsHistogramHeadline(): String = when (this) {
    RatingBand.GENEROUS -> "A generous rater"
    RatingBand.SUPERFAN -> "Hard to disappoint"
    RatingBand.CENTRIST -> "Right down the middle"
    RatingBand.EXACTING -> "Tough to impress"
    RatingBand.POLARISED -> "Love it or leave it"
    RatingBand.NEW_READER -> "Too early to tell"
}
