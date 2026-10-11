package nl.rhaydus.softcover.feature.profile.presentation.collector

import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution

internal data class ChartsInput(
    val genres: GenreBreakdown,
    val demographics: AuthorDemographics,
    val ratings: RatingsDistribution,
    val hideUntaggedAuthors: Boolean,
)
