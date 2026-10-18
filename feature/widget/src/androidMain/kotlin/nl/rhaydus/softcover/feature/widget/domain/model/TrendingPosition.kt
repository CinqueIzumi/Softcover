package nl.rhaydus.softcover.feature.widget.domain.model

/** Which feed an instance shows and the page it is on; the page may be stale against a shorter list. */
internal data class TrendingPosition(
    val feed: TrendingFeed,
    val page: Int,
)
