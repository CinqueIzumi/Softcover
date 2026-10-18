package nl.rhaydus.softcover.feature.widget.domain.model

/** Where a feed stands in getting its first data, as the widget needs to tell a loader from an empty list. */
internal enum class TrendingFeedFetchStatus {
    /** The feed has no data yet and its last fetch has not failed. */
    Loading,

    /** The feed has no data and its last fetch failed. */
    Failed,

    /** The feed has had data, so an empty ranking is the real answer. */
    Settled,
}
