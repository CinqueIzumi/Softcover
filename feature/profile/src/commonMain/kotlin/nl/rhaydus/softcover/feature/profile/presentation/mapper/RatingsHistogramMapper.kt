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
        "Half-stars count. Most of yours land between four and five — you round up when a book earns it."

    RatingBand.SUPERFAN ->
        "Your average sits north of four and a half — you finish what you start, and it usually earns the " +
            "top shelf."

    RatingBand.CENTRIST ->
        "Your ratings cluster around three and a half — steady and considered, rarely swinging to an " +
            "extreme."

    RatingBand.EXACTING ->
        "Your average runs low — a five-star read is rare here, which makes the ones you give out mean " +
            "something."

    RatingBand.POLARISED ->
        "Your ratings split into two camps, low and high, with little in between — a book either wins " +
            "you over or it doesn't."

    RatingBand.NEW_READER ->
        "Not enough ratings yet to say much about your taste — a few more books will fill this in."
}

private fun RatingBand.toRatingsHistogramHeadline(): String = when (this) {
    RatingBand.GENEROUS -> "A generous but honest hand"
    RatingBand.SUPERFAN -> "Easily, happily impressed"
    RatingBand.CENTRIST -> "You keep to the middle of the shelf"
    RatingBand.EXACTING -> "A hard star to earn"
    RatingBand.POLARISED -> "Love it or leave it"
    RatingBand.NEW_READER -> "Still finding your scale"
}
