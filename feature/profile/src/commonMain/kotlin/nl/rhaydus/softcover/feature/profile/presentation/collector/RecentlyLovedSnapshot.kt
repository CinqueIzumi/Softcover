package nl.rhaydus.softcover.feature.profile.presentation.collector

import nl.rhaydus.softcover.core.profile.domain.model.LovedBook

/** The state slices the "Recently loved" cards are derived from. */
internal data class RecentlyLovedSnapshot(
    val books: List<LovedBook>,
    val isLoading: Boolean,
)
