package nl.rhaydus.softcover.feature.profile.presentation.mapper

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand

private const val RATINGS_HISTOGRAM_EYEBROW = "How you score"

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
