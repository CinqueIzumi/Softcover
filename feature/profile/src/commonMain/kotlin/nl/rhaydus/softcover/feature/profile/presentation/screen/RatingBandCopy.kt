package nl.rhaydus.softcover.feature.profile.presentation.screen

/**
 * The headline + caption pair [RatingsHistogramSection] renders for a given
 * [nl.rhaydus.softcover.core.profile.domain.model.RatingBand] — authored here in presentation, mapped
 * from the domain-classified band via the `RatingBand.toCopy()` extension in `ProfileShelf.kt`.
 */
internal data class RatingBandCopy(
    val headline: String,
    val caption: String,
)
