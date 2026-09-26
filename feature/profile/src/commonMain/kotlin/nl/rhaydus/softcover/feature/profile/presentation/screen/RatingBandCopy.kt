package nl.rhaydus.softcover.feature.profile.presentation.screen

import nl.rhaydus.softcover.feature.profile.presentation.screen.section.RatingsHistogramSection

/**
 * The headline + caption pair [RatingsHistogramSection] renders for a given
 * [nl.rhaydus.softcover.core.profile.domain.model.RatingBand] — authored here in presentation, mapped
 * from the domain-classified band via the `RatingBand.toCopy()` extension.
 */
internal data class RatingBandCopy(
    val headline: String,
    val caption: String,
)
