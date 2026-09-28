package nl.rhaydus.softcover.feature.reading.presentation.collector

/** The state slices both Reading mastheads and their refresh action are derived from. */
internal data class MastheadSnapshot(
    val bookCount: Int,
    val averageProgress: Float?,
    val isLoading: Boolean,
)
